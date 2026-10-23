package com.example.myapplication.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface ContactSyncDao {

    @Query("SELECT * FROM contact_sync WHERE localId = :localId")
    suspend fun find(localId: String): ContactSyncEntity?

    @Upsert
    suspend fun save(link: ContactSyncEntity)
    @Query("SELECT * FROM contact_sync")
    suspend fun getAll(): List<ContactSyncEntity>

    @Query("DELETE FROM contact_sync WHERE localId = :localId")
    suspend fun delete(localId: String)
}
