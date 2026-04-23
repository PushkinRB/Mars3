package com.roman.mars.data.repository

import android.content.ContentResolver
import android.provider.ContactsContract
import com.roman.mars.data.model.Contact
import com.roman.mars.data.phone.PhoneNormalizer

class ContactRepository(
    private val contentResolver: ContentResolver
) {

    fun loadContacts(): List<Contact> {
        val contacts = mutableListOf<Contact>()
        val seen = mutableSetOf<String>()
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )
        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            null,
            null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )
        cursor?.use {
            val idIndex = it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val nameIndex = it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val phoneIndex = it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
            while (it.moveToNext()) {
                val id = it.getString(idIndex).orEmpty()
                val name = it.getString(nameIndex).orEmpty().ifBlank { "Без имени" }
                val phone = it.getString(phoneIndex).orEmpty()
                val normalized = PhoneNormalizer.normalize(phone)
                if (normalized.isBlank()) continue
                val uniqueKey = "$id:$normalized"
                if (!seen.add(uniqueKey)) continue
                contacts.add(
                    Contact(
                        id = uniqueKey,
                        name = name,
                        phone = phone,
                        phoneNormalized = normalized
                    )
                )
            }
        }
        return contacts
    }
}