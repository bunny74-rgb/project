package com.example.myapplication.ViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.ContactDatabase
import com.example.myapplication.data.ContactEntity
import com.example.myapplication.data.ContactWithPhones
import com.example.myapplication.repository.ContactRepository
import kotlinx.coroutines.launch

class ContactViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository: ContactRepository

    val allContacts: LiveData<List<ContactWithPhones>>

    init {
        val database = ContactDatabase.getDatabase(application)

        repository = ContactRepository(database)

        allContacts = repository.allContacts.asLiveData()
    }

    // Thêm liên hệ
    fun insert(name: String, numbers: List<String>) {
        val validNumbers = cleanNumbers(numbers)

        if (name.isBlank() || validNumbers.isEmpty()) return

        viewModelScope.launch {
            val contact = ContactEntity(
                name = name.trim()
            )

            repository.insert(contact, validNumbers)
        }
    }

    // Sửa liên hệ
    fun update(
        contact: ContactEntity,
        name: String,
        numbers: List<String>
    ) {
        val validNumbers = cleanNumbers(numbers)

        if (name.isBlank() || validNumbers.isEmpty()) return

        viewModelScope.launch {
            val updatedContact = contact.copy(
                name = name.trim()
            )

            repository.update(updatedContact, validNumbers)
        }
    }

    // Xóa liên hệ
    fun delete(contact: ContactEntity) {
        viewModelScope.launch {
            repository.delete(contact)
        }
    }

    // Bỏ khoảng trắng, số trống và số trùng
    private fun cleanNumbers(numbers: List<String>): List<String> {
        return numbers
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
    }
}
