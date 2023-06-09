package ru.flocator.app.community.adapters


import ru.flocator.app.community.data_classes.Friends

interface FriendActionListener {
    fun onPersonOpenProfile(user: Friends)
}