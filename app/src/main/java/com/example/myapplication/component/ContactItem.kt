package com.example.myapplication.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.ContactWithPhones

@Composable
fun ContactItem(
    item: ContactWithPhones,
    onContactClick: (ContactWithPhones) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onContactClick(item) }
            .padding(12.dp)
    ) {
        Text(
            text = item.contact.name,
            fontWeight = FontWeight.Bold
        )

        item.phones.forEach { phone ->
            Text(
                text = phone.number,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
