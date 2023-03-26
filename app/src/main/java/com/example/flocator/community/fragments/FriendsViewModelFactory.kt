package com.example.flocator.community.fragments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

@Suppress("UNCHECKED_CAST")
class FriendsViewModelFactory(
    private val personRepository: PersonRepository
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return FriendsViewModel(personRepository) as T
    }
}