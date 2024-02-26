package ru.flocator.feature_community.internal.ui.adapters

import ru.flocator.feature_community.internal.data.model.UserItem

internal interface UserNewFriendActionListener {
    fun onPersonOpenProfile(user: UserItem)
    fun onPersonAccept(user: UserItem)
    fun onPersonCancel(user: UserItem)
}