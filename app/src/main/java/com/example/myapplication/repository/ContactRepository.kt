package com.example.myapplication.repository

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.provider.ContactsContract
import androidx.core.content.ContextCompat
import androidx.room.withTransaction
import com.example.myapplication.data.ContactDatabase
import com.example.myapplication.data.ContactEntity
import com.example.myapplication.data.ContactSyncEntity
import com.example.myapplication.data.ContactWithPhones
import com.example.myapplication.data.PhoneEntity
import com.example.myapplication.model.LinkedSystemContact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class ContactRepository(
    private val database: ContactDatabase,
    context: Context
) {
    private val appContext = context.applicationContext
    private val resolver = appContext.contentResolver

    private val contactDao = database.contactDao()
    private val syncDao = database.contactSyncDao()

    private val reader = SystemContactReader(resolver)
    private val writer = SystemContactWriter(resolver)

    // Các thao tác đồng bộ chạy lần lượt.
    private val syncMutex = Mutex()

    val allContacts: Flow<List<ContactWithPhones>> =
        contactDao.getContacts()

    private fun checkPermissions() {
        val canRead = ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED

        val canWrite = ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.WRITE_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED

        if (!canRead || !canWrite) {
            throw SecurityException(
                "Cần quyền đọc và ghi danh bạ"
            )
        }
    }

    private fun cleanNumbers(numbers: List<String>): List<String> {
        return numbers
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
    }

    // Lưu liên hệ, số điện thoại và liên kết ID cùng nhau.
    private suspend fun saveLocal(
        contact: ContactEntity,
        numbers: List<String>,
        rawContactId: Long,
        isNew: Boolean
    ) {
        database.withTransaction {
            if (isNew) {
                contactDao.insertContact(contact)
            } else {
                contactDao.updateContact(contact)
                contactDao.deletePhones(contact.id)
            }

            contactDao.insertPhones(
                cleanNumbers(numbers).map { number ->
                    PhoneEntity(
                        contactId = contact.id,
                        number = number
                    )
                }
            )

            syncDao.save(
                ContactSyncEntity(
                    localId = contact.id,
                    rawContactId = rawContactId
                )
            )
        }
    }

    suspend fun insert(
        contact: ContactEntity,
        numbers: List<String>
    ) {
        syncMutex.withLock {
            withContext(Dispatchers.IO) {
                checkPermissions()

                val validNumbers = cleanNumbers(numbers)

                val rawId = writer.insertContact(
                    contact.name,
                    validNumbers
                )

                saveLocal(
                    contact = contact,
                    numbers = validNumbers,
                    rawContactId = rawId,
                    isNew = true
                )
            }
        }
    }

    suspend fun update(
        contact: ContactEntity,
        numbers: List<String>
    ) {
        syncMutex.withLock {
            withContext(Dispatchers.IO) {
                checkPermissions()

                val validNumbers = cleanNumbers(numbers)
                val link = syncDao.find(contact.id)

                val rawId = if (link == null || !reader.rawContactExists(link.rawContactId)) {
                    writer.insertContact(
                        contact.name,
                        validNumbers
                    )
                } else {
                    writer.updateContact(
                        rawContactId = link.rawContactId,
                        name = contact.name,
                        numbers = validNumbers
                    )

                    link.rawContactId
                }

                saveLocal(
                    contact = contact,
                    numbers = validNumbers,
                    rawContactId = rawId,
                    isNew = false
                )
            }
        }
    }

    suspend fun delete(contact: ContactEntity) {
        syncMutex.withLock {
            withContext(Dispatchers.IO) {
                checkPermissions()

                val link = syncDao.find(contact.id)

                if (link != null) {
                    val uri = ContentUris.withAppendedId(
                        ContactsContract.RawContacts.CONTENT_URI,
                        link.rawContactId
                    )

                    resolver.delete(uri, null, null)

                    check(!reader.rawContactExists(link.rawContactId)) {
                        "Không xóa được liên hệ hệ thống"
                    }
                }

                database.withTransaction {
                    contactDao.deleteContact(contact.id)
                    syncDao.delete(contact.id)
                }
            }
        }
    }

    // Giữ tương thích với hàm xuất hiện tại trong ViewModel.
    suspend fun exportToSystem(
        contact: ContactEntity,
        numbers: List<String>
    ) {
        update(contact, numbers)
    }

    private fun sameContent(
        local: ContactWithPhones,
        system: LinkedSystemContact
    ): Boolean {
        val localNumbers = cleanNumbers(
            local.phones.map { it.number }
        ).sorted()

        val systemNumbers = cleanNumbers(
            system.numbers
        ).sorted()

        return local.contact.name == system.name &&
                localNumbers == systemNumbers
    }

    suspend fun synchronize() {
        syncMutex.withLock {
            withContext(Dispatchers.IO) {
                checkPermissions()

                // Đọc hoàn tất trước khi quyết định cập nhật/xóa Room.
                val systemContacts = reader.readForSync()
                    .associateBy { it.rawContactId }

                val localContacts = contactDao.getContacts()
                    .first()
                    .associateBy { it.contact.id }

                val links = syncDao.getAll()

                val linkedLocalIds = links
                    .map { it.localId }
                    .toSet()

                val linkedRawIds = links
                    .map { it.rawContactId }
                    .toMutableSet()

                // 1. Cập nhật các liên hệ đã liên kết: Hệ thống → Room.
                for (link in links) {
                    val system = systemContacts[link.rawContactId]
                    val local = localContacts[link.localId]

                    if (system == null) {
                        // RawContact đã bị xóa bên hệ thống.
                        database.withTransaction {
                            contactDao.deleteContact(link.localId)
                            syncDao.delete(link.localId)
                        }
                    } else if (
                        local == null || !sameContent(local, system)
                    ) {
                        saveLocal(
                            contact = ContactEntity(
                                id = link.localId,
                                name = system.name
                            ),
                            numbers = system.numbers,
                            rawContactId = system.rawContactId,
                            isNew = local == null
                        )
                    }
                }

                // 2. Đưa liên hệ cũ chưa liên kết của app sang hệ thống.
                for ((localId, local) in localContacts) {
                    if (localId in linkedLocalIds) continue

                    val numbers = cleanNumbers(
                        local.phones.map { it.number }
                    )

                    if (
                        local.contact.name.isBlank() ||
                        numbers.isEmpty()
                    ) {
                        continue
                    }

                    val rawId = writer.insertContact(
                        local.contact.name,
                        numbers
                    )

                    syncDao.save(
                        ContactSyncEntity(
                            localId = localId,
                            rawContactId = rawId
                        )
                    )

                    linkedRawIds.add(rawId)
                }

                // 3. Nhập liên hệ hệ thống chưa có trong app.
                for ((rawId, system) in systemContacts) {
                    if (rawId in linkedRawIds) continue

                    // App hiện quản lý những liên hệ có số điện thoại.
                    if (system.numbers.isEmpty()) continue

                    val contact = ContactEntity(
                        name = system.name
                    )

                    saveLocal(
                        contact = contact,
                        numbers = system.numbers,
                        rawContactId = rawId,
                        isNew = true
                    )
                }
            }
        }
    }
}
