package com.roman.mars.data.supabase
import android.util.Log import io.github.jan.supabase.auth.auth
object SessionCheck {
    suspend fun logSession() {
        try {
            val session = SupabaseProvider.client.auth.currentSessionOrNull()
            if (session != null) {
                val userId = session.user?.id ?: "unknown"
                Log.d("SessionCheck", "Session exists, userId=$userId")
            } else {
                Log.e("SessionCheck", "No session found")
            }
        } catch (e: Exception) {
            Log.e("SessionCheck", "Session check failed", e)
        }
    }
}