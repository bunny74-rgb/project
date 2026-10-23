package com.example.myapplication.navigation

import android.net.Uri

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier

import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myapplication.ViewModel.ContactViewModel
import com.example.myapplication.component.ContactDetailScreen
import com.example.myapplication.component.ContactEditScreen
import com.example.myapplication.component.ContactScreen

@Composable
fun AppNavigation(
    contactViewModel: ContactViewModel
) {
    val navController = rememberNavController()

    // null nghĩa là Room chưa trả dữ liệu lần đầu.
    val contacts by contactViewModel.allContacts.observeAsState()

    NavHost(
        navController = navController,
        startDestination = "contacts"
    ) {
        // Màn hình danh sách
        composable("contacts") {
            ContactScreen(
                contactViewModel = contactViewModel,
                onContactClick = { id ->
                    contactViewModel.clearError()

                    navController.navigate(
                        "detail/${Uri.encode(id)}"
                    ) {
                        launchSingleTop = true
                    }
                }
            )
        }

        // Màn hình chi tiết
        composable(
            route = "detail/{id}",
            arguments = listOf(
                navArgument("id") {
                    type = NavType.StringType
                }
            )
        ) { entry ->
            val id = entry.arguments?.getString("id")

            val item = contacts?.find {
                it.contact.id == id
            }

            if (item == null) {
                ContactUnavailable(
                    loading = contacts == null,
                    onBack = {
                        navController.popBackStack()
                    }
                )
            } else {
                ContactDetailScreen(
                    item = item,
                    isSaving = contactViewModel.isSaving,
                    errorMessage = contactViewModel.errorMessage,

                    onBack = {
                        navController.popBackStack()
                    },

                    onEdit = {
                        contactViewModel.clearError()

                        navController.navigate(
                            "edit/${Uri.encode(item.contact.id)}"
                        ) {
                            launchSingleTop = true
                        }
                    },

                    onDelete = {
                        contactViewModel.delete(
                            contact = item.contact,
                            onSuccess = {
                                if (
                                    navController.currentBackStackEntry == entry
                                ) {
                                    navController.popBackStack(
                                        route = "contacts",
                                        inclusive = false
                                    )
                                }
                            }
                        )
                    },
                )
            }
        }

        // Màn hình chỉnh sửa
        composable(
            route = "edit/{id}",
            arguments = listOf(
                navArgument("id") {
                    type = NavType.StringType
                }
            )
        ) { entry ->
            val id = entry.arguments?.getString("id")

            val item = contacts?.find {
                it.contact.id == id
            }

            if (item == null) {
                ContactUnavailable(
                    loading = contacts == null,
                    onBack = {
                        navController.popBackStack()
                    }
                )
            } else {
                ContactEditScreen(
                    item = item,
                    isSaving = contactViewModel.isSaving,
                    errorMessage = contactViewModel.errorMessage,

                    onBack = {
                        navController.popBackStack()
                    },

                    onSave = { name, numbers ->
                        contactViewModel.update(
                            contact = item.contact,
                            name = name,
                            numbers = numbers,
                            onSuccess = {
                                if (
                                    navController.currentBackStackEntry == entry
                                ) {
                                    navController.popBackStack()
                                }
                            }
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun ContactUnavailable(
    loading: Boolean,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .safeDrawingPadding()
            .padding(16.dp)
    ) {
        Text(
            text = if (loading) {
                "Đang tải dữ liệu..."
            } else {
                "Liên hệ không còn tồn tại."
            }
        )

        TextButton(onClick = onBack) {
            Text("Quay lại")
        }
    }
}
