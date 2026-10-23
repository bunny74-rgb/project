package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contact_sync")
data class ContactSyncEntity(
    @PrimaryKey
    val localId: String,
    val rawContactId: Long
)
