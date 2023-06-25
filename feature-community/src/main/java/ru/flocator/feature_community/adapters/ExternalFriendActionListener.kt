package ru.flocator.feature_community.adapters


import ru.flocator.core_dto.user.UserExternalFriends

interface ExternalFriendActionListener {
    fun onPersonOpenProfile(user: UserExternalFriends)
}