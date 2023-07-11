package ru.flocator.feature_community.internal.adapters

import ru.flocator.feature_community.internal.domain.user.FriendRequests

internal interface UserNewFriendActionListener {
    fun onPersonOpenProfile(user: FriendRequests)
    fun onPersonAccept(user: FriendRequests)
    fun onPersonCancel(user: FriendRequests)
}