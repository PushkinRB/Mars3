package com.roman.mars.ui.auth

import com.roman.mars.data.supabase.ProfileDto

class SessionUserRepository(
    private val authRepository: AuthRepository = AuthRepository(),
    private val profileRepository: ProfileRepository = ProfileRepository()
) {
    fun currentUserId(): String? {
        return authRepository.currentUserId()
    }

    suspend fun loadCurrentProfile(): ProfileDto? {
        val userId = authRepository.currentUserId() ?: return null
        return profileRepository.loadMyProfile(userId)
    }
}
