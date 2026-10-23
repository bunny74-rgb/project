package com.example.myapplication.ViewModel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.ContactDatabase
import com.example.myapplication.data.ContactEntity
import com.example.myapplication.repository.ContactRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import com.example.myapplication.model.SystemContact
import com.example.myapplication.repository.SystemContactReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.myapplication.repository.SystemContactWriter
import com.example.myapplication.data.ContactWithPhones
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.ContactsContract
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
class ContactViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = ContactRepository(
        database = ContactDatabase.getDatabase(application),
        context = application
    )

    val allContacts = repository.allContacts.asLiveData()

    var isSaving by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun clearError() {
        errorMessage = null
    }

    fun insert(
        name: String,
        numbers: List<String>,
        onSuccess: () -> Unit = {}
    ) {
        val validNumbers = cleanNumbers(numbers)

        if (name.isBlank() || validNumbers.isEmpty()) {
            errorMessage = "Nhập tên và ít nhất một số điện thoại"
            return
        }

        save(onSuccess) {
            repository.insert(
                ContactEntity(name = name.trim()),
                validNumbers
            )
        }
    }

    fun update(
        contact: ContactEntity,
        name: String,
        numbers: List<String>,
        onSuccess: () -> Unit = {}
    ) {
        val validNumbers = cleanNumbers(numbers)

        if (name.isBlank() || validNumbers.isEmpty()) {
            errorMessage = "Nhập tên và ít nhất một số điện thoại"
            return
        }

        save(onSuccess) {
            repository.update(
                contact.copy(name = name.trim()),
                validNumbers
            )
        }
    }

    fun delete(
        contact: ContactEntity,
        onSuccess: () -> Unit = {}
    ) {
        save(onSuccess) {
            repository.delete(contact)
        }
    }

    private fun save(
        onSuccess: () -> Unit,
        action: suspend () -> Unit
    ) {
        if (isSaving) return

        isSaving = true
        errorMessage = null

        viewModelScope.launch {
            try {
                action()
                onSuccess()
            } catch (e: CancellationException) {
                throw e
            } catch (e: SecurityException) {
                android.util.Log.e(
                    "ContactSave",
                    "Thiếu quyền truy cập danh bạ",
                    e
                )

                errorMessage = "Cần cấp quyền đọc và ghi danh bạ"
            } catch (e: Exception) {
                android.util.Log.e(
                    "ContactSave",
                    "Lưu liên hệ thất bại",
                    e
                )

                errorMessage =
                    "Không thể lưu dữ liệu. Vui lòng thử lại."
            } finally {
                isSaving = false
            }
        }
    }

    private fun cleanNumbers(numbers: List<String>): List<String> {
        return numbers
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
    }

    private val systemContactReader = SystemContactReader(
        application.contentResolver
    )

    var systemContacts by mutableStateOf<List<SystemContact>>(emptyList())
        private set

    var isReadingContacts by mutableStateOf(false)
        private set

    var systemContactsMessage by mutableStateOf("")
        private set

    fun readSystemContacts() {
        if (isReadingContacts) return

        isReadingContacts = true
        systemContactsMessage = "Đang đọc..."

        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    systemContactReader.readContacts()
                }

                systemContacts = result
                systemContactsMessage =
                    "Đã đọc ${result.size} liên hệ có số điện thoại"
            } catch (e: CancellationException) {
                throw e
            } catch (e: SecurityException) {
                systemContactsMessage = "Chưa có quyền đọc danh bạ"
            } catch (e: Exception) {
                systemContactsMessage = "Không đọc được danh bạ"
            } finally {
                isReadingContacts = false
            }
        }
    }

    private val systemContactWriter = SystemContactWriter(
        application.contentResolver
    )

    var isExporting by mutableStateOf(false)
        private set

    fun exportToSystemContacts(
        item: ContactWithPhones,
        onSuccess: () -> Unit
    ) {
        if (isExporting) return

        isExporting = true
        errorMessage = null

        viewModelScope.launch {
            try {
                val numbers = item.phones
                    .map { it.number.trim() }
                    .filter { it.isNotBlank() }
                    .distinct()

                require(item.contact.name.isNotBlank() && numbers.isNotEmpty()) {
                    "Liên hệ phải có tên và số điện thoại"
                }

                repository.exportToSystem(
                    contact = item.contact,
                    numbers = numbers

                )

                onSuccess()
            } catch (e: CancellationException) {
                throw e
            } catch (e: SecurityException) {
                errorMessage = "Chưa có quyền ghi danh bạ"
            } catch (e: Exception) {
                errorMessage =
                    "Không thể xuất hoặc cập nhật liên hệ hệ thống"
            } finally {
                isExporting = false
            }
        }
    }
    var syncMessage by mutableStateOf(
        "Chưa bật đồng bộ tự động"
    )
        private set

    private val contactResolver =
        application.contentResolver

    private var automaticSyncEnabled = false
    private var observerRegistered = false

    private var syncRequested = false
    private var syncJob: Job? = null

    private val contactObserver = object : ContentObserver(
        Handler(Looper.getMainLooper())
    ) {
        override fun onChange(selfChange: Boolean) {
            requestAutomaticSync()
        }
    }

    fun startAutomaticSync() {
        try {
            if (!observerRegistered) {
                contactResolver.registerContentObserver(
                    ContactsContract.AUTHORITY_URI,
                    true,
                    contactObserver
                )

                observerRegistered = true
            }

            automaticSyncEnabled = true

            // Mỗi lần quay lại app đều kiểm tra lại dữ liệu.
            requestAutomaticSync()
        } catch (e: SecurityException) {
            stopAutomaticSync()
            syncMessage = "Cần cấp quyền đọc và ghi danh bạ"
        }
    }

    fun stopAutomaticSync() {
        automaticSyncEnabled = false
        syncRequested = false

        if (observerRegistered) {
            contactResolver.unregisterContentObserver(
                contactObserver
            )

            observerRegistered = false
        }
    }

    private fun requestAutomaticSync() {
        if (!automaticSyncEnabled) return

        syncRequested = true

        // Nếu đang đồng bộ, yêu cầu mới sẽ được xử lý ở vòng sau.
        if (syncJob?.isActive == true) return

        syncJob = viewModelScope.launch {
            while (automaticSyncEnabled && syncRequested) {
                syncRequested = false

                // Gom các thông báo phát sinh gần nhau.
                delay(400)

                if (!automaticSyncEnabled) break

                syncMessage = "Đang đồng bộ..."

                try {
                    repository.synchronize()

                    syncMessage = "Đã đồng bộ danh bạ"
                } catch (e: CancellationException) {
                    throw e
                } catch (e: SecurityException) {
                    stopAutomaticSync()

                    syncMessage =
                        "Đồng bộ tạm dừng: thiếu quyền danh bạ"
                } catch (e: Exception) {
                    syncMessage =
                        "Đồng bộ chưa thành công. " +
                                "Ứng dụng sẽ kiểm tra lại khi bạn mở lại."
                }
            }
        }
    }

    override fun onCleared() {
        stopAutomaticSync()
        super.onCleared()
    }

}



