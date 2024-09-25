package com.example.recursosnativos

data class User(
    val id: Long = 0,
    val email: String,
    val name: String,
    val comment: String,
    val imagePath: String?
)
