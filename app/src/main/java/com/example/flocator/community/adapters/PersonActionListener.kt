package com.example.flocator.community.adapters

import com.example.flocator.community.data_classes.Person

interface PersonActionListener {
    fun onPersonGetId(person: Person)
    fun onPersonAccept(person: Person)
    fun onPersonCancel(person: Person)
}