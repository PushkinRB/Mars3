package com.roman.mars.ui.auth

import android.util.Log
import com.roman.mars.data.supabase.SupabaseProvider
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email

class AuthRepository(
    private val profileRepository: ProfileRepository = ProfileRepository()
) {

    private val client = SupabaseProvider.client
    suspend fun signUp(email: String, password: String) {
        client.auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
        val user = client.auth.currentUserOrNull()
        Log.d("AuthRepository", "signUp currentUser=${user?.id}")
        if (user != null) {
            val displayName = email.substringBefore("@").ifBlank { "Пользователь" }
            profileRepository.upsertProfile(
                id = user.id,
                email = user.email,
                displayName = displayName
            )
        }
    }
    suspend fun signIn(email: String, password: String) {
        client.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
        val user = client.auth.currentUserOrNull()
        Log.d("AuthRepository", "signIn currentUser=${user?.id}")
        if (user != null) {
            val displayName = user.email?.substringBefore("@").orEmpty().ifBlank { "Пользователь" }
            profileRepository.upsertProfile(
                id = user.id,
                email = user.email,
                displayName = displayName
            )
        }
    }
    suspend fun signOut() {
        client.auth.signOut()
    }
    fun isAuthorized(): Boolean {
        val session = client.auth.currentSessionOrNull()
        Log.d("AuthRepository", "isAuthorized session=${session != null}")
        return session != null
    }
    fun currentUserId(): String? {
        val userId = client.auth.currentUserOrNull()?.id
        Log.d("AuthRepository", "currentUserId=$userId")
        return userId
    }
    fun currentUserEmail(): String? {
        val email = client.auth.currentUserOrNull()?.email
        Log.d("AuthRepository", "currentUserEmail=$email")
        return email
    }
    fun refreshAuthState(): Pair<Boolean, String?> {
        val session = client.auth.currentSessionOrNull()
        val user = client.auth.currentUserOrNull()
        Log.d(
            "AuthRepository",
            "refreshAuthState session=${session != null}, userId=${user?.id}, email=${user?.email}"
        )
        return (session != null) to user?.email
    }
}