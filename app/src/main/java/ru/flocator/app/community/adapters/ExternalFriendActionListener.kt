package ru.flocator.app.community.adapters


import ru.flocator.core_dto.user.UserExternalFriends

interface ExternalFriendActionListener {
    fun onPersonOpenProfile(user: UserExternalFriends)
}