package com.example.flocator.community.adapters

import com.example.flocator.community.data_classes.User

interface UserNewFriendActionListener {
    fun onPersonOpenProfile(user: User)
    fun onPersonAccept(user: User)
    fun onPersonCancel(user: User)
}