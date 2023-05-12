package com.example.flocator.community.adapters

import com.example.flocator.community.data_classes.FriendRequests
import com.example.flocator.community.data_classes.User
import com.example.flocator.community.data_classes.UserExternal

interface UserNewFriendActionListener {
    fun onPersonOpenProfile(user: FriendRequests)
    fun onPersonAccept(user: FriendRequests)
    fun onPersonCancel(user: FriendRequests)
}