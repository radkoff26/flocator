package com.example.flocator.community.adapters

import com.example.flocator.community.data_classes.FriendRequests

interface UserNewFriendActionListener {
    fun onPersonOpenProfile(user: FriendRequests)
    fun onPersonAccept(user: FriendRequests)
    fun onPersonCancel(user: FriendRequests)
}