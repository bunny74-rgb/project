package com.example.myapplication.repository

import android.content.ContentProviderOperation
import android.content.ContentResolver
import android.provider.ContactsContract
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.CommonDataKinds.StructuredName
import android.content.ContentUris
class SystemContactWriter(
    private val resolver: ContentResolver
) {
    fun insertContact(name: String, numbers: List<String>): Long {
        require(name.isNotBlank())
        require(numbers.isNotEmpty())

        val operations = arrayListOf<ContentProviderOperation>()

        // Thao tác số 0: tạo liên hệ
        operations.add(
            ContentProviderOperation
                .newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build()
        )

        // Thêm tên vào liên hệ vừa tạo
        operations.add(
            ContentProviderOperation
                .newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(
                    ContactsContract.Data.RAW_CONTACT_ID,
                    0
                )
                .withValue(
                    ContactsContract.Data.MIMETYPE,
                    StructuredName.CONTENT_ITEM_TYPE
                )
                .withValue(StructuredName.DISPLAY_NAME, name)
                .build()
        )

        // Thêm từng số điện thoại
        numbers.forEach { number ->
            operations.add(
                ContentProviderOperation
                    .newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(
                        ContactsContract.Data.RAW_CONTACT_ID,
                        0
                    )
                    .withValue(
                        ContactsContract.Data.MIMETYPE,
                        Phone.CONTENT_ITEM_TYPE
                    )
                    .withValue(Phone.NUMBER, number)
                    .withValue(Phone.TYPE, Phone.TYPE_OTHER)
                    .build()
            )
        }
        val results = resolver.applyBatch(
            ContactsContract.AUTHORITY,
            operations
        )

        val rawContactUri = results[0].uri
            ?: error("Không nhận được ID liên hệ vừa tạo")

        return ContentUris.parseId(rawContactUri)
    }

    fun updateContact(
        rawContactId: Long,
        name: String,
        numbers: List<String>
    ) {
        require(name.isNotBlank())
        require(numbers.isNotEmpty())

        val operations = arrayListOf<ContentProviderOperation>()

        // Xóa tên cũ của RawContact này (nếu có)
        operations.add(
            ContentProviderOperation
                .newDelete(ContactsContract.Data.CONTENT_URI)
                .withSelection(
                    "${ContactsContract.Data.RAW_CONTACT_ID} = ? " +
                            "AND ${ContactsContract.Data.MIMETYPE} = ?",
                    arrayOf(
                        rawContactId.toString(),
                        StructuredName.CONTENT_ITEM_TYPE
                    )
                )
                .build()
        )

        // Thêm tên mới
        operations.add(
            ContentProviderOperation
                .newInsert(ContactsContract.Data.CONTENT_URI)
                .withValue(
                    ContactsContract.Data.RAW_CONTACT_ID,
                    rawContactId
                )
                .withValue(
                    ContactsContract.Data.MIMETYPE,
                    StructuredName.CONTENT_ITEM_TYPE
                )
                .withValue(StructuredName.DISPLAY_NAME, name)
                .build()
        )

        // Chỉ xóa các số điện thoại thuộc RawContact này
        operations.add(
            ContentProviderOperation
                .newDelete(ContactsContract.Data.CONTENT_URI)
                .withSelection(
                    "${ContactsContract.Data.RAW_CONTACT_ID} = ? " +
                            "AND ${ContactsContract.Data.MIMETYPE} = ?",
                    arrayOf(
                        rawContactId.toString(),
                        Phone.CONTENT_ITEM_TYPE
                    )
                )
                .build()
        )

        // Thêm lại danh sách số mới
        numbers.forEach { number ->
            operations.add(
                ContentProviderOperation
                    .newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValue(
                        ContactsContract.Data.RAW_CONTACT_ID,
                        rawContactId
                    )
                    .withValue(
                        ContactsContract.Data.MIMETYPE,
                        Phone.CONTENT_ITEM_TYPE
                    )
                    .withValue(Phone.NUMBER, number)
                    .withValue(Phone.TYPE, Phone.TYPE_OTHER)
                    .build()
            )
        }

        resolver.applyBatch(
            ContactsContract.AUTHORITY,
            operations
        )
    }
}
