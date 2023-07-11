package ru.flocator.core_dto.auth

data class UserRegistrationDto(
    val firstName: String,
    val lastName: String,
    val email: String,
    val login: String,
    val password: String
)
