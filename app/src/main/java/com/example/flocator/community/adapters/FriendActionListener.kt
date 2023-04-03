package com.example.flocator.community.adapters

import com.example.flocator.community.data_classes.Person

interface FriendActionListener {
    fun onPersonOpenProfile(person: Person)
}