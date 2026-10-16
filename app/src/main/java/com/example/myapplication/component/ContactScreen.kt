package com.example.myapplication.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.ViewModel.ContactViewModel
import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactScreen(
    contactViewModel: ContactViewModel,
    onContactClick: (String) -> Unit
) {
    // Quan sát dữ liệu từ Room qua LiveData
    val contacts by contactViewModel.allContacts
        .observeAsState(emptyList())

    var nameInput by rememberSaveable {
        mutableStateOf("")
    }

    var phoneInput by rememberSaveable {
        mutableStateOf("")
    }
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            contactViewModel.readSystemContacts()
        } else {
            Toast.makeText(
                context,
                "Bạn chưa cấp quyền đọc danh bạ",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Danh bạ điện thoại") }
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .imePadding()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                enabled = !contactViewModel.isReadingContacts,
                onClick = {
                    val granted = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.READ_CONTACTS
                    ) == PackageManager.PERMISSION_GRANTED

                    if (granted) {
                        contactViewModel.readSystemContacts()
                    } else {
                        permissionLauncher.launch(
                            Manifest.permission.READ_CONTACTS
                        )
                    }
                }
            ) {
                Text("Đọc danh bạ hệ thống")
            }
            Text(contactViewModel.systemContactsMessage)

            if (contactViewModel.systemContacts.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 180.dp)
                ) {
                    items(
                        items = contactViewModel.systemContacts,
                        key = { it.id }
                    ) { contact ->
                        Column(
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Text("Tên: ${contact.name}")

                            contact.numbers.forEach { number ->
                                Text(number)
                            }
                        }
                    }
                }
            }

            Text("Danh bạ đã lưu trong ứng dụng")
            ContactList(
                contacts = contacts,
                onContactClick = {
                        item ->
                    onContactClick(item.contact.id)
                },
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = nameInput,
                onValueChange = { nameInput = it },
                label = { Text("Tên liên hệ") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = phoneInput,
                onValueChange = { phoneInput = it },
                label = { Text("Các số điện thoại") },
                placeholder = {
                    Text("0901234567, 0987654321")
                },
                supportingText = {
                    Text("Ngăn cách các số bằng dấu phẩy")
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            contactViewModel.errorMessage?.let { message ->
                Text(text = message)
            }
            Button(

                onClick = {
                    val numbers = phoneInput
                        .split(",")
                        .map { it.trim() }
                        .filter { it.isNotBlank() }

                    if (nameInput.isNotBlank() && numbers.isNotEmpty()) {
                        contactViewModel.insert(
                            name = nameInput,
                            numbers = numbers,
                            onSuccess = {
                                nameInput = ""
                                phoneInput = ""
                            }
                        )
                    }
                },
                enabled = !contactViewModel.isSaving &&
                        nameInput.isNotBlank() &&
                        phoneInput.isNotBlank()
            ) {
                Text("Thêm liên hệ")
            }
        }
    }
}
