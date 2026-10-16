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
import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactDetailScreen(
    item: ContactWithPhones,
    isSaving: Boolean,
    errorMessage: String?,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    isExporting: Boolean,
    onExport: () -> Unit
) {
    var showDeleteDialog by rememberSaveable(item.contact.id) {
        mutableStateOf(false)
    }

    val context = LocalContext.current

    val writePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            onExport()
        } else {
            Toast.makeText(
                context,
                "Chưa được cấp quyền ghi danh bạ",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết liên hệ") },
                navigationIcon = {
                    TextButton(
                        onClick = onBack,
                        enabled = !isSaving
                    ) {
                        Text("Quay lại")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = item.contact.name,
                style = MaterialTheme.typography.headlineSmall
            )

            Text("Số điện thoại:")

            item.phones.forEach { phone ->
                Text(phone.number)
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onEdit,
                    enabled = !isSaving
                ) {
                    Text("Sửa")
                }

                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    enabled = !isSaving
                ) {
                    Text("Xóa")
                }
            }
            Button(
                enabled = !isSaving && !isExporting,
                onClick = {
                    val granted = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.WRITE_CONTACTS
                    ) == PackageManager.PERMISSION_GRANTED

                    if (granted) {
                        onExport()
                    } else {
                        writePermissionLauncher.launch(
                            Manifest.permission.WRITE_CONTACTS
                        )
                    }
                }
            ) {
                Text(
                    if (isExporting) "Đang xuất..."
                    else "Xuất sang danh bạ"
                )
            }

            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }

    if (showDeleteDialog) {
        ContactDialog(
            contactName = item.contact.name,
            isSaving = isSaving,
            onConfirm = {
                showDeleteDialog = false
                onDelete()
            },
            onDismiss = {
                showDeleteDialog = false
            }
        )
    }
}
