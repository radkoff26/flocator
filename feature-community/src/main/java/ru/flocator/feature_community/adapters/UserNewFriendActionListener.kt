package ru.flocator.feature_community.adapters

import ru.flocator.core_dto.user.FriendRequests

interface UserNewFriendActionListener {
    fun onPersonOpenProfile(user: FriendRequests)
    fun onPersonAccept(user: FriendRequests)
    fun onPersonCancel(user: FriendRequests)
}