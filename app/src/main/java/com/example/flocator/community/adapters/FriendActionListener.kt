package com.example.flocator.community.adapters


import com.example.flocator.community.data_classes.Friends

interface FriendActionListener {
    fun onPersonOpenProfile(user: Friends)
}