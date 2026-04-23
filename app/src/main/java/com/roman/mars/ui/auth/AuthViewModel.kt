package com.roman.mars.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository(),
    private val sessionUserRepository: SessionUserRepository = SessionUserRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
    init {
        initialize()
    }
    private fun initialize() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isInitializing = true,
                    error = null
                )
            }
            delay(500)
            val authState = repository.refreshAuthState()
            val isAuthorized = authState.first
            val email = authState.second
            val profile = if (isAuthorized) sessionUserRepository.loadCurrentProfile() else null
            _uiState.update {
                it.copy(
                    isInitializing = false,
                    isAuthorized = isAuthorized,
                    currentUserEmail = email,
                    currentUserDisplayName = profile?.displayName,
                    error = null
                )
            }
        }
    }
    fun onEmailChanged(value: String) {
        _uiState.update { it.copy(email = value, error = null) }
    }
    fun onPasswordChanged(value: String) {
        _uiState.update { it.copy(password = value, error = null) }
    }
    fun onRememberMeChanged(value: Boolean) {
        _uiState.update { it.copy(rememberMe = value, error = null) }
    }
    fun applyRememberMe(value: Boolean) {
        _uiState.update { it.copy(rememberMe = value) }
    }
    fun applySavedCredentials(email: String, password: String) {
        _uiState.update {
            it.copy(
                email = email,
                password = password
            )
        }
    }
    fun togglePasswordVisibility() {
        _uiState.update {
            it.copy(isPasswordVisible = !it.isPasswordVisible)
        }
    }
    fun toggleMode() {
        _uiState.update {
            it.copy(
                isLoginMode = !it.isLoginMode,
                error = null
            )
        }
    }
    fun submit() {
        val email = uiState.value.email.trim()
        val password = uiState.value.password
        val isLoginMode = uiState.value.isLoginMode
        if (email.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(error = "Введите email и пароль") }
            return
        }
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    error = null
                )
            }
            try {
                if (isLoginMode) {
                    repository.signIn(email, password)
                } else {
                    repository.signUp(email, password)
                }
                delay(300)
                val authState = repository.refreshAuthState()
                val isAuthorized = authState.first
                val currentEmail = authState.second
                val profile = if (isAuthorized) sessionUserRepository.loadCurrentProfile() else null
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isAuthorized = isAuthorized,
                        currentUserEmail = currentEmail,
                        currentUserDisplayName = profile?.displayName,
                        error = if (!isAuthorized && !isLoginMode) {
                            "Регистрация выполнена. Если вход не произошёл автоматически, войдите вручную"
                        } else {
                            null
                        }
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Ошибка авторизации"
                    )
                }
            }
        }
    }
    fun signOut() {
        viewModelScope.launch {
            try {
                repository.signOut()
                _uiState.update {
                    it.copy(
                        isAuthorized = false,
                        currentUserEmail = null,
                        currentUserDisplayName = null,
                        password = "",
                        error = null,
                        isInitializing = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "Не удалось выйти")
                }
            }
        }
    }
}