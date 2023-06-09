package ru.flocator.app.community.adapters

import ru.flocator.app.community.data_classes.FriendRequests

interface UserNewFriendActionListener {
    fun onPersonOpenProfile(user: FriendRequests)
    fun onPersonAccept(user: FriendRequests)
    fun onPersonCancel(user: FriendRequests)
}