package com.roman.mars.ui.auth

class SessionRepository(
    private val authRepository: AuthRepository = AuthRepository()
) {
    fun currentUserId(): String? = authRepository.currentUserId()
}