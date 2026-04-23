package com.roman.mars.data.supabase

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.util.UUID

object SupabaseSmokeTest {

    private const val CHAT_ID = "0973b970-d09e-429a-8061-700fdc2779e3"
    private const val SENDER_ID = "bb641e1d-f9d7-4650-9896-18f0328dc05f"
    private val httpClient = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
        }
    }
    fun run() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val newClientId = UUID.randomUUID().toString()
                val requestBody = buildJsonArray {
                    add(
                        buildJsonObject {
                            put("chat_id", CHAT_ID)
                            put("sender_id", SENDER_ID)
                            put("client_id", newClientId)
                            put("text", "Привет из Android в Supabase")
                        }
                    )
                }
                val insertResponse = httpClient.post("${SupabaseProvider.SUPABASE_URL}/rest/v1/messages") {
                    header("apikey", SupabaseProvider.SUPABASE_KEY)
                    header("Authorization", "Bearer ${SupabaseProvider.SUPABASE_KEY}")
                    header("Prefer", "return=representation")
                    contentType(ContentType.Application.Json)
                    setBody(requestBody)
                }
                Log.d("SupabaseSmokeTest", "INSERT HTTP ${insertResponse.status.value}")
                Log.d("SupabaseSmokeTest", "INSERT Body: ${insertResponse.bodyAsText()}")
                val selectResponse = httpClient.get("${SupabaseProvider.SUPABASE_URL}/rest/v1/messages?select=*&chat_id=eq.$CHAT_ID&order=created_at.asc") {
                    header("apikey", SupabaseProvider.SUPABASE_KEY)
                    header("Authorization", "Bearer ${SupabaseProvider.SUPABASE_KEY}")
                }
                Log.d("SupabaseSmokeTest", "SELECT HTTP ${selectResponse.status.value}")
                Log.d("SupabaseSmokeTest", "SELECT Body: ${selectResponse.bodyAsText()}")
            } catch (e: Exception) {
                Log.e("SupabaseSmokeTest", "Messages insert/select failed", e)
            }
        }
    }
}