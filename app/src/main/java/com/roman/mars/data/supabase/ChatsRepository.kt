package com.roman.mars.data.supabase

import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc

class ChatsRepository {

    suspend fun loadChats(): List<MyChatRpcDto> {
        return SupabaseProvider.client.postgrest.rpc(
            function = "get_my_chats_with_members"
        ).decodeList<MyChatRpcDto>()
    }
}
