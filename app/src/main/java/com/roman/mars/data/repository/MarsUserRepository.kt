package com.roman.mars.data.repository

import com.roman.mars.data.model.MarsUser
import com.roman.mars.data.supabase.FindProfilesByPhonesDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

class MarsUserRepository(
    private val supabase: SupabaseClient
) {

    suspend fun findByPhones(phones: List<String>): List<MarsUser> {
        if (phones.isEmpty()) return emptyList()
        val uniquePhones = phones
            .filter { it.isNotBlank() }
            .distinct()
        if (uniquePhones.isEmpty()) return emptyList()
        val result = supabase.postgrest.rpc(
            function = "find_profiles_by_phones",
            parameters = buildJsonObject {
                put(
                    "input_phones",
                    JsonArray(uniquePhones.map { JsonPrimitive(it) })
                )
            }
        )
        val rows = result.decodeList<FindProfilesByPhonesDto>()
        return rows.map {
            MarsUser(
                id = it.id,
                email = it.email,
                displayName = it.displayName,
                phone = it.phone,
                phoneNormalized = it.phoneNormalized
            )
        }
    }
}