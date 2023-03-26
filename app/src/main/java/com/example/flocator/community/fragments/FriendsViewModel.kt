package com.example.flocator.community.fragments


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.flocator.community.data_classes.Person

class FriendsViewModel(
    private val personRepository: PersonRepository
) : ViewModel() {

    private val _friends =
        MutableLiveData<List<Person>>()
    val friends: LiveData<List<Person>>
        get() = _friends

    fun getFriends(){
        val friends = personRepository.getPersons()
        _friends.value = friends
    }
}