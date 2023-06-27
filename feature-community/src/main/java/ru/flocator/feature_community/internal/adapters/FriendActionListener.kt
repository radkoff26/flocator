package ru.flocator.feature_community.internal.adapters


import ru.flocator.core_dto.user.Friends

internal interface FriendActionListener {
    fun onPersonOpenProfile(user: Friends)
}