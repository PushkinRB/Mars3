package com.roman.mars.data.repository

import com.roman.mars.data.model.Contact
import com.roman.mars.data.model.MatchedContact

class ContactMatcherRepository(
    private val marsUserRepository: MarsUserRepository
) {

    suspend fun matchContacts(contacts: List<Contact>): List<MatchedContact> {
        if (contacts.isEmpty()) return emptyList()
        val phones = contacts.map { it.phoneNormalized }.distinct()
        val marsUsers = marsUserRepository.findByPhones(phones)
        val byPhone = marsUsers.associateBy { it.phoneNormalized }
        return contacts.map { contact ->
            MatchedContact(
                contact = contact,
                marsUser = byPhone[contact.phoneNormalized]
            )
        }
    }
}