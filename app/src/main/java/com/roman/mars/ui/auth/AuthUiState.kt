package com.roman.mars.ui.auth

data class AuthUiState(
    val email: String = "",
val password: String = "",
val isLoginMode: Boolean = true,
val isLoading: Boolean = false,
val isInitializing: Boolean = true,
val error: String? = null,
val isAuthorized: Boolean = false,
val currentUserEmail: String? = null,
val currentUserDisplayName: String? = null,
val rememberMe: Boolean = true,
val isPasswordVisible: Boolean = false
)