package com.roman.mars.data.model

data class MatchedContact(
    val contact: Contact,
    val marsUser: MarsUser?
) {
    val isRegisteredInMars: Boolean
        get() = marsUser != null
}