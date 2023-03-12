package com.example.flocator

interface PersonActionListener {
    fun onPersonGetId(person: Person)
    fun onPersonAccept(person: Person)
    fun onPersonCancel(person: Person)
}