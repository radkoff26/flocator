package ru.flocator.feature_community.internal.adapters

import ru.flocator.core_dto.user.FriendRequests

internal interface UserNewFriendActionListener {
    fun onPersonOpenProfile(user: FriendRequests)
    fun onPersonAccept(user: FriendRequests)
    fun onPersonCancel(user: FriendRequests)
}