package com.example.flocator.community.adapters

import com.example.flocator.community.data_classes.User
import com.example.flocator.community.data_classes.UserExternal

interface UserNewFriendActionListener {
    fun onPersonOpenProfile(user: UserExternal)
    fun onPersonAccept(user: UserExternal)
    fun onPersonCancel(user: UserExternal)
}