package com.roman.mars.data.supabase

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.time.Instant

class ChatsRepository {

    private val httpClient = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
        }
    }
    suspend fun loadChats(): List<ChatDto> {
        return httpClient.get("${SupabaseProvider.SUPABASE_URL}/rest/v1/chats?select=*&order=created_at.desc") {
            header("apikey", SupabaseProvider.SUPABASE_KEY)
            header("Authorization", "Bearer ${SupabaseProvider.SUPABASE_KEY}")
        }.body()
    }
    suspend fun createChat(title: String, createdBy: String): ChatDto {
        val response = httpClient.post("${SupabaseProvider.SUPABASE_URL}/rest/v1/chats") {
            header("apikey", SupabaseProvider.SUPABASE_KEY)
            header("Authorization", "Bearer ${SupabaseProvider.SUPABASE_KEY}")
            header("Prefer", "return=representation")
            contentType(ContentType.Application.Json)
            setBody(
                buildJsonArray {
                    add(
                        buildJsonObject {
                            put("type", "direct")
                            put("title", title)
                            put("created_by", createdBy)
                        }
                    )
                }
            )
        }
        Log.d("ChatsRepository", "CREATE CHAT HTTP ${response.status.value}")
        Log.d("ChatsRepository", "CREATE CHAT Body: ${response.bodyAsText()}")
        val body: List<ChatDto> = response.body()
        return body.first()
    }
    suspend fun updateChatTitle(chatId: String, title: String) {
        val response = httpClient.patch("${SupabaseProvider.SUPABASE_URL}/rest/v1/chats?id=eq.$chatId") {
            header("apikey", SupabaseProvider.SUPABASE_KEY)
            header("Authorization", "Bearer ${SupabaseProvider.SUPABASE_KEY}")
            header("Prefer", "return=representation")
            contentType(ContentType.Application.Json)
            setBody(
                buildJsonObject {
                    put("title", title)
                    put("updated_at", Instant.now().toString())
                }
            )
        }
        Log.d("ChatsRepository", "UPDATE CHAT HTTP ${response.status.value}")
        Log.d("ChatsRepository", "UPDATE CHAT Body: ${response.bodyAsText()}")
    }
    suspend fun deleteChat(chatId: String) {
        val response = httpClient.delete("${SupabaseProvider.SUPABASE_URL}/rest/v1/chats?id=eq.$chatId") {
            header("apikey", SupabaseProvider.SUPABASE_KEY)
            header("Authorization", "Bearer ${SupabaseProvider.SUPABASE_KEY}")
        }
        Log.d("ChatsRepository", "DELETE CHAT HTTP ${response.status.value}")
        Log.d("ChatsRepository", "DELETE CHAT Body: ${response.bodyAsText()}")
    }
}