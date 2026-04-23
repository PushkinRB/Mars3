package com.roman.mars.data.supabase

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable

data class FindProfilesByPhonesDto(
    @SerialName("id")
val id: String,
@SerialName("email")
val email: String? = null,
@SerialName("display_name")
val displayName: String? = null,
@SerialName("phone")
val phone: String? = null,
@SerialName("phone_normalized")
val phoneNormalized: String
)