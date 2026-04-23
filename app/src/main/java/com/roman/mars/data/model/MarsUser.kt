package com.roman.mars.data.model

data class MarsUser(
    val id: String,
    val email: String?,
    val displayName: String?,
    val phone: String?,
    val phoneNormalized: String
)