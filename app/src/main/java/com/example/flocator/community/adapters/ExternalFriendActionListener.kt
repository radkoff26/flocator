package com.example.flocator.community.adapters


import com.example.flocator.community.data_classes.UserExternal
import com.example.flocator.community.data_classes.UserExternalFriends

interface ExternalFriendActionListener {
    fun onPersonOpenProfile(user: UserExternalFriends)
}