package ru.flocator.app.community.adapters


import ru.flocator.app.community.data_classes.UserExternalFriends

interface ExternalFriendActionListener {
    fun onPersonOpenProfile(user: UserExternalFriends)
}