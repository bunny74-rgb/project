package com.example.myapplication

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.ViewModel.ContactViewModel
import com.example.myapplication.navigation.AppNavigation
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private lateinit var contactViewModel: ContactViewModel

    private val permissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {
            if (hasContactsPermissions()) {
                contactViewModel.startAutomaticSync()
            } else {
                contactViewModel.stopAutomaticSync()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        contactViewModel = ViewModelProvider(this)[
            ContactViewModel::class.java
        ]

        setContent {
            MyApplicationTheme {
                AppNavigation(
                    contactViewModel = contactViewModel
                )
            }
        }

        if (
            savedInstanceState == null &&
            !hasContactsPermissions()
        ) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.WRITE_CONTACTS
                )
            )
        }
    }

    override fun onStart() {
        super.onStart()

        if (hasContactsPermissions()) {
            contactViewModel.startAutomaticSync()
        } else {
            contactViewModel.stopAutomaticSync()
        }
    }

    override fun onStop() {
        contactViewModel.stopAutomaticSync()
        super.onStop()
    }

    private fun hasContactsPermissions(): Boolean {
        val permissions = arrayOf(
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS
        )

        return permissions.all { permission ->
            ContextCompat.checkSelfPermission(
                this,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
}
