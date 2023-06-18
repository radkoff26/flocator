package ru.flocator.app.community.adapters


import ru.flocator.core_dto.user.Friends

interface FriendActionListener {
    fun onPersonOpenProfile(user: Friends)
}