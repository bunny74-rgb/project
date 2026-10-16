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
class ContactViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = ContactRepository(
        ContactDatabase.getDatabase(application)
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
            } catch (e: Exception) {
                errorMessage = "Không thể lưu dữ liệu. Vui lòng thử lại."
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

                withContext(Dispatchers.IO) {
                    systemContactWriter.insertContact(
                        name = item.contact.name,
                        numbers = numbers
                    )
                }

                onSuccess()
            } catch (e: CancellationException) {
                throw e
            } catch (e: SecurityException) {
                errorMessage = "Chưa có quyền ghi danh bạ"
            } catch (e: Exception) {
                errorMessage = "Không thể xuất liên hệ sang danh bạ"
            } finally {
                isExporting = false
            }
        }
    }

}


