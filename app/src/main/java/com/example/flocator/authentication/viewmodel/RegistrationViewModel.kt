package com.example.flocator.authentication.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RegistrationViewModel : ViewModel() {
    val nameData = MutableLiveData<Pair<String, String>>() // Фамилия, Имя
    val loginEmailData = MutableLiveData<Pair<String, String>>() // Login, Email
}