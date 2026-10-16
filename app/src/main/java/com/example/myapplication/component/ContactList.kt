package com.example.myapplication.component

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.myapplication.data.ContactWithPhones

@Composable
fun ContactList(
    contacts: List<ContactWithPhones>,
    onContactClick: (ContactWithPhones) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        items(
            items = contacts,
            key = { it.contact.id }
        ) { item ->
            ContactItem(
                item = item,
                onContactClick = onContactClick
            )
        }
    }
}
