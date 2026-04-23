package com.roman.mars.data.supabase

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
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
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.time.Instant
import java.util.UUID

class MessagesRepository {

    private val httpClient = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
        }
    }
    suspend fun loadMessages(chatId: String): List<MessageDto> {
        return httpClient.get("${SupabaseProvider.SUPABASE_URL}/rest/v1/messages?select=*&chat_id=eq.$chatId&deleted_at=is.null&order=created_at.asc") {
            header("apikey", SupabaseProvider.SUPABASE_KEY)
            header("Authorization", "Bearer ${SupabaseProvider.SUPABASE_KEY}")
        }.body()
    }
    suspend fun sendMessage(chatId: String, senderId: String, text: String): MessageDto {
        val request = listOf(
            SendMessageRequest(
                chatId = chatId,
                senderId = senderId,
                clientId = UUID.randomUUID().toString(),
                text = text
            )
        )
        val response: List<MessageDto> = httpClient.post("${SupabaseProvider.SUPABASE_URL}/rest/v1/messages") {
            header("apikey", SupabaseProvider.SUPABASE_KEY)
            header("Authorization", "Bearer ${SupabaseProvider.SUPABASE_KEY}")
            header("Prefer", "return=representation")
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
        return response.first()
    }
    suspend fun deleteMessage(messageId: String) {
        val response = httpClient.patch("${SupabaseProvider.SUPABASE_URL}/rest/v1/messages?id=eq.$messageId") {
            header("apikey", SupabaseProvider.SUPABASE_KEY)
            header("Authorization", "Bearer ${SupabaseProvider.SUPABASE_KEY}")
            header("Prefer", "return=representation")
            contentType(ContentType.Application.Json)
            setBody(
                buildJsonObject {
                    put("deleted_at", Instant.now().toString())
                }
            )
        }
        Log.d("MessagesRepository", "DELETE HTTP ${response.status.value}")
        Log.d("MessagesRepository", "DELETE Body: ${response.bodyAsText()}")
    }
    suspend fun editMessage(messageId: String, newText: String) {
        val response = httpClient.patch("${SupabaseProvider.SUPABASE_URL}/rest/v1/messages?id=eq.$messageId") {
            header("apikey", SupabaseProvider.SUPABASE_KEY)
            header("Authorization", "Bearer ${SupabaseProvider.SUPABASE_KEY}")
            header("Prefer", "return=representation")
            contentType(ContentType.Application.Json)
            setBody(
                buildJsonObject {
                    put("text", newText)
                    put("updated_at", Instant.now().toString())
                }
            )
        }
        Log.d("MessagesRepository", "EDIT HTTP ${response.status.value}")
        Log.d("MessagesRepository", "EDIT Body: ${response.bodyAsText()}")
    }
}