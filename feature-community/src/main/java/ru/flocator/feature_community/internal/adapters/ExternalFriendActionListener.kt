package ru.flocator.feature_community.internal.adapters


import ru.flocator.feature_community.internal.domain.user.UserExternalFriends

internal interface ExternalFriendActionListener {
    fun onPersonOpenProfile(user: UserExternalFriends)
}