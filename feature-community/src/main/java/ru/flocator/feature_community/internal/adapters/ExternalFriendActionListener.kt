package ru.flocator.feature_community.internal.adapters


import ru.flocator.core_dto.user.UserExternalFriends

internal interface ExternalFriendActionListener {
    fun onPersonOpenProfile(user: UserExternalFriends)
}