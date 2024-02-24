package ru.flocator.feature_community.internal.adapters

import ru.flocator.feature_community.internal.data.UserItem


internal interface FriendActionListener {
    fun onPersonOpenProfile(user: UserItem)
}