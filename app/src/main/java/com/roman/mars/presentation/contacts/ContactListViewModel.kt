package com.roman.mars.presentation.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roman.mars.data.model.MatchedContact
import com.roman.mars.data.repository.ContactMatcherRepository
import com.roman.mars.data.repository.ContactRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ContactListViewModel(
    private val contactRepository: ContactRepository,
    private val contactMatcherRepository: ContactMatcherRepository
) : ViewModel() {

    private val _contacts = MutableStateFlow<List<MatchedContact>>(emptyList())
    val contacts: StateFlow<List<MatchedContact>> = _contacts.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    fun loadContacts() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val localContacts = contactRepository.loadContacts()
                _contacts.value = contactMatcherRepository.matchContacts(localContacts)
            } finally {
                _isLoading.value = false
            }
        }
    }
}