package ru.flocator.feature_community.adapters


import ru.flocator.core_dto.user.Friends

interface FriendActionListener {
    fun onPersonOpenProfile(user: Friends)
}