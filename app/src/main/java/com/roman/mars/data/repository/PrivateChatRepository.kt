package com.roman.mars.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

class PrivateChatRepository(
    private val supabase: SupabaseClient
) {

    suspend fun createPrivateChat(otherUserId: String, chatTitle: String): String {
        val result = supabase.postgrest.rpc(
            function = "create_private_chat",
            parameters = buildJsonObject {
                put("other_user_id", JsonPrimitive(otherUserId))
                put("chat_title", JsonPrimitive(chatTitle))
            }
        )
        return result.decodeAs<String>().trim('"')
    }
}