package com.example.myapplication.repository

import androidx.room.withTransaction
import com.example.myapplication.data.ContactDatabase
import com.example.myapplication.data.ContactEntity
import com.example.myapplication.data.ContactWithPhones
import com.example.myapplication.data.PhoneEntity
import kotlinx.coroutines.flow.Flow

class ContactRepository(
    private val database: ContactDatabase
) {
    private val contactDao = database.contactDao()

    // Theo dõi danh sách liên hệ
    val allContacts: Flow<List<ContactWithPhones>> =
        contactDao.getContacts()

    // Thêm liên hệ cùng các số điện thoại
    suspend fun insert(
        contact: ContactEntity,
        numbers: List<String>
    ) {
        database.withTransaction {
            contactDao.insertContact(contact)

            val phones = numbers.map { number ->
                PhoneEntity(
                    contactId = contact.id,
                    number = number
                )
            }

            contactDao.insertPhones(phones)
        }
    }

    // Sửa liên hệ và thay danh sách số điện thoại
    suspend fun update(
        contact: ContactEntity,
        numbers: List<String>
    ) {
        database.withTransaction {
            contactDao.updateContact(contact)
            contactDao.deletePhones(contact.id)

            val phones = numbers.map { number ->
                PhoneEntity(
                    contactId = contact.id,
                    number = number
                )
            }

            contactDao.insertPhones(phones)
        }
    }

    // Xóa liên hệ; CASCADE tự xóa các số đi kèm
    suspend fun delete(contact: ContactEntity) {
        contactDao.deleteContact(contact.id)
    }
}
