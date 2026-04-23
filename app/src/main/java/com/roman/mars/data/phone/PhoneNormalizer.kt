package com.roman.mars.data.phone

object PhoneNormalizer {

    fun normalize(raw: String?): String {
        if (raw.isNullOrBlank()) return ""
        val digits = raw.filter { it.isDigit() }
        if (digits.isBlank()) return ""
        return when {
            digits.length == 11 && digits.startsWith("8") -> {
                "7" + digits.drop(1)
            }
            digits.length == 10 -> {
                "7$digits"
            }
            digits.length == 11 && digits.startsWith("7") -> {
                digits
            }
            else -> digits
        }
    }
}