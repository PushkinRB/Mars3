package com.roman.mars.ui.auth

import com.roman.mars.data.supabase.ProfileDto
import com.roman.mars.data.supabase.SupabaseProvider
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class ProfileRepository {

    private val client = SupabaseProvider.client
    suspend fun upsertProfile(
        id: String,
        email: String?,
        displayName: String?
    ) {
        client
            .from("profiles")
            .upsert(
                buildJsonObject {
                    put("id", id)
                    put("email", email)
                    put("display_name", displayName)
                }
            )
    }
    suspend fun loadMyProfile(userId: String): ProfileDto? {
        return client
            .from("profiles")
            .select {
                filter {
                    eq("id", userId)
                }
            }
            .decodeSingleOrNull<ProfileDto>()
    }
}