package ru.flocator.feature_community.internal.adapters


import ru.flocator.feature_community.internal.domain.user.Friends

internal interface FriendActionListener {
    fun onPersonOpenProfile(user: Friends)
}