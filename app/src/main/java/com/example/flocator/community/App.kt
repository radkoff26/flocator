package com.example.flocator.community

import android.app.Application
import com.example.flocator.community.fragments.PersonRepository


class App : Application() {
    val personService = PersonRepository()
}