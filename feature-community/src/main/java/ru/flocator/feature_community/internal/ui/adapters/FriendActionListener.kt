package ru.flocator.feature_community.internal.ui.adapters

import ru.flocator.feature_community.internal.data.model.UserItem


internal interface FriendActionListener {
    fun onPersonOpenProfile(user: UserItem)
}