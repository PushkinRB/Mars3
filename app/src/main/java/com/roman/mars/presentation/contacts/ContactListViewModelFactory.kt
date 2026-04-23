package com.roman.mars.presentation.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.roman.mars.data.repository.ContactMatcherRepository
import com.roman.mars.data.repository.ContactRepository

class ContactListViewModelFactory(
    private val contactRepository: ContactRepository,
    private val contactMatcherRepository: ContactMatcherRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ContactListViewModel::class.java)) {
            return ContactListViewModel(
                contactRepository = contactRepository,
                contactMatcherRepository = contactMatcherRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}