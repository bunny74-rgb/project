package com.example.myapplication.repository

import android.content.ContentProviderOperation
import android.content.ContentResolver
import android.provider.ContactsContract
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.CommonDataKinds.StructuredName

class SystemContactWriter(
    private val resolver: ContentResolver
) {
    fun insertContact(name: String, numbers: List<String>) {
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

        resolver.applyBatch(
            ContactsContract.AUTHORITY,
            operations
        )
    }
}
