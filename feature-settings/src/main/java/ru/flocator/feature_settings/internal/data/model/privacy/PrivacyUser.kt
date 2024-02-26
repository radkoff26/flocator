package ru.flocator.feature_settings.internal.data.model.privacy

internal data class PrivacyUser(
    val userId: Long,
    val avatarUri: String?,
    val firstName: String,
    val lastName: String,
    var isChecked: Boolean
)