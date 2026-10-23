package com.example.myapplication.model

data class LinkedSystemContact(
    val rawContactId: Long,
    val name: String,
    val numbers: List<String>
)
