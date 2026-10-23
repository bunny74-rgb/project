package com.example.myapplication.repository

import android.content.ContentResolver
import android.provider.ContactsContract.CommonDataKinds.Phone
import com.example.myapplication.model.SystemContact
import android.content.ContentUris
import android.provider.ContactsContract
import android.provider.ContactsContract.CommonDataKinds.StructuredName
import com.example.myapplication.model.LinkedSystemContact
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
        ) ?: return emptyList()

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

    fun rawContactExists(rawContactId: Long): Boolean {
        val uri = ContentUris.withAppendedId(
            ContactsContract.RawContacts.CONTENT_URI,
            rawContactId
        )

        val cursor = resolver.query(
            uri,
            arrayOf(ContactsContract.RawContacts._ID),
            "${ContactsContract.RawContacts.DELETED} = 0",
            null,
            null
        ) ?: return false

        return cursor.use {
            it.moveToFirst()
        }
    }

    fun readForSync(): List<LinkedSystemContact> {
        val rawIds = mutableListOf<Long>()

        val rawCursor = resolver.query(
            ContactsContract.RawContacts.CONTENT_URI,
            arrayOf(ContactsContract.RawContacts._ID),
            "${ContactsContract.RawContacts.DELETED} = 0",
            null,
            null
        ) ?: return emptyList()

        rawCursor.use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(
                ContactsContract.RawContacts._ID
            )

            while (cursor.moveToNext()) {
                rawIds.add(cursor.getLong(idIndex))
            }
        }

        return rawIds.mapNotNull { rawId ->
            var name = ""
            val numbers = mutableListOf<String>()

            val dataCursor = resolver.query(
                ContactsContract.Data.CONTENT_URI,
                arrayOf(
                    ContactsContract.Data.MIMETYPE,
                    ContactsContract.Data.DATA1
                ),
                "${ContactsContract.Data.RAW_CONTACT_ID} = ? AND " +
                        "${ContactsContract.Data.MIMETYPE} IN (?, ?)",
                arrayOf(
                    rawId.toString(),
                    StructuredName.CONTENT_ITEM_TYPE,
                    Phone.CONTENT_ITEM_TYPE
                ),
                null
            ) ?: return@mapNotNull null

            dataCursor.use { cursor ->
                val mimeIndex = cursor.getColumnIndexOrThrow(
                    ContactsContract.Data.MIMETYPE
                )

                val valueIndex = cursor.getColumnIndexOrThrow(
                    ContactsContract.Data.DATA1
                )

                while (cursor.moveToNext()) {
                    val mimeType = cursor.getString(mimeIndex)

                    val value = cursor.getString(valueIndex)
                        ?.trim()
                        .orEmpty()

                    when (mimeType) {
                        StructuredName.CONTENT_ITEM_TYPE -> {
                            name = value
                        }

                        Phone.CONTENT_ITEM_TYPE -> {
                            if (value.isNotBlank()) {
                                numbers.add(value)
                            }
                        }
                    }
                }
            }

            // Liên hệ có thể vừa bị xóa trong lúc đang đọc.
            if (!rawContactExists(rawId)) {
                null
            } else {
                LinkedSystemContact(
                    rawContactId = rawId,
                    name = name.ifBlank { "Không có tên" },
                    numbers = numbers.distinct()
                )
            }
        }
    }
}
