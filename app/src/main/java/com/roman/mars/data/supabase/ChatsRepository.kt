package com.roman.mars.data.supabase
import android.util.Log import io.github.jan.supabase.postgrest.postgrest import io.github.jan.supabase.postgrest.query.Columns import io.github.jan.supabase.postgrest.rpc
class ChatsRepository {
    suspend fun loadChats(): List<MyChatRpcDto> {
        Log.d("ChatsRepository", "Calling get_my_chats_with_members")
        return try {
            val result = SupabaseProvider.client.postgrest.rpc(
                function = "get_my_chats_with_members"
            ).decodeList<MyChatRpcDto>()
            Log.d("ChatsRepository", "RPC returned ${result.size} chats")
            result
        } catch (e: Exception) {
            Log.e("ChatsRepository", "RPC failed", e)
            throw e
        }
    }

    suspend fun deleteChat(chatId: String) {
        SupabaseProvider.client.postgrest["chat_members"]
            .delete {
                filter {
                    eq("chat_id", chatId)
                }
            }

        SupabaseProvider.client.postgrest["chats"]
            .delete {
                filter {
                    eq("id", chatId)
                }
            }
    }
}