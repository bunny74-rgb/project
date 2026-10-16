package com.example.myapplication.repository

import android.content.ContentResolver
import android.provider.ContactsContract.CommonDataKinds.Phone
import com.example.myapplication.model.SystemContact

class SystemContactReader(
    private val resolver: ContentResolver
) {
    fun readContacts(): List<SystemContact> {

        val contacts = linkedMapOf<String, SystemContact>()

        val projection = arrayOf(
            Phone.CONTACT_ID,
            Phone.DISPLAY_NAME,
            Phone.NUMBER
        )

        val cursor = resolver.query(
            Phone.CONTENT_URI,
            projection,
            null,
            null,
            "${Phone.DISPLAY_NAME} ASC"
        ) ?: error("Không đọc được danh bạ hệ thống")

        cursor.use {
            val idIndex = it.getColumnIndexOrThrow(Phone.CONTACT_ID)
            val nameIndex = it.getColumnIndexOrThrow(Phone.DISPLAY_NAME)
            val numberIndex = it.getColumnIndexOrThrow(Phone.NUMBER)

            while (it.moveToNext()) {
                val id = it.getString(idIndex)
                val name = it.getString(nameIndex) ?: "Không có tên"
                val number = it.getString(numberIndex)?.trim().orEmpty()

                if (number.isNotBlank()) {
                    val existing = contacts[id]

                    if (existing == null) {
                        contacts[id] = SystemContact(
                            id = id,
                            name = name,
                            numbers = listOf(number)
                        )
                    } else {
                        contacts[id] = existing.copy(
                            numbers = (existing.numbers + number).distinct()
                        )
                    }
                }
            }
        }

        return contacts.values.toList()
    }
}
