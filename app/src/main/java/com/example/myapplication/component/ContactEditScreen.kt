package com.example.myapplication.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.ContactWithPhones

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactEditScreen(
    item: ContactWithPhones,
    isSaving: Boolean,
    errorMessage: String?,
    onBack: () -> Unit,
    onSave: (String, List<String>) -> Unit
) {
    var nameInput by rememberSaveable(item.contact.id) {
        mutableStateOf(item.contact.name)
    }

    var phoneInput by rememberSaveable(item.contact.id) {
        mutableStateOf(
            item.phones.joinToString(", ") { it.number }
        )
    }

    val numbers = phoneInput
        .split(",")
        .map { it.trim() }
        .filter { it.isNotBlank() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sửa liên hệ") },
                navigationIcon = {
                    TextButton(
                        onClick = onBack,
                        enabled = !isSaving
                    ) {
                        Text("Hủy")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = nameInput,
                onValueChange = { nameInput = it },
                label = { Text("Tên liên hệ") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving,
                singleLine = true
            )

            OutlinedTextField(
                value = phoneInput,
                onValueChange = { phoneInput = it },
                label = { Text("Các số điện thoại") },
                supportingText = {
                    Text("Ngăn cách các số bằng dấu phẩy")
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving,
                singleLine = true
            )

            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Button(
                onClick = {
                    onSave(nameInput, numbers)
                },
                enabled = !isSaving &&
                        nameInput.isNotBlank() &&
                        numbers.isNotEmpty()
            ) {
                Text(if (isSaving) "Đang lưu..." else "Lưu")
            }
        }
    }
}
