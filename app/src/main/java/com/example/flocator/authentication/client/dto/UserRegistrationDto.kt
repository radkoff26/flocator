package com.example.flocator.authentication.client.dto

data class UserRegistrationDto(
    val firstName: String,
    val lastName: String,
    val email: String,
    val login: String,
    val password: String
)
