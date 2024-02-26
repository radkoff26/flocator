package ru.flocator.feature_settings.internal.data.model.friend

internal data class BlackListUser(
    val userId: Long,
    val avatarUri: String?,
    val firstName: String,
    val lastName: String
)