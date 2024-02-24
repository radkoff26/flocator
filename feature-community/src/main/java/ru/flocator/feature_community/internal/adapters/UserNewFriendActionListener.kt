package ru.flocator.feature_community.internal.adapters

import ru.flocator.feature_community.internal.data.UserItem

internal interface UserNewFriendActionListener {
    fun onPersonOpenProfile(user: UserItem)
    fun onPersonAccept(user: UserItem)
    fun onPersonCancel(user: UserItem)
}