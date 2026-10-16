package com.example.myapplication.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {

    // Lấy danh sách liên hệ kèm số điện thoại
    @Transaction
    @Query("SELECT * FROM contacts ORDER BY name")
    fun getContacts(): Flow<List<ContactWithPhones>>

    // Thêm liên hệ
    @Insert
    suspend fun insertContact(contact: ContactEntity)

    // Thêm các số điện thoại
    @Insert
    suspend fun insertPhones(phones: List<PhoneEntity>)

    // Sửa thông tin liên hệ
    @Update
    suspend fun updateContact(contact: ContactEntity)

    // Xóa các số điện thoại của một liên hệ
    @Query("DELETE FROM phones WHERE contactId = :contactId")
    suspend fun deletePhones(contactId: String)

    // Xóa liên hệ
    @Query("DELETE FROM contacts WHERE id = :id")
    suspend fun deleteContact(id: String)
}
