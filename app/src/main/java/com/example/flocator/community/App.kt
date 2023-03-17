package com.example.flocator.community

import android.app.Application
import com.example.flocator.community.fragments.PersonService

class App : Application() {
    val personService = PersonService()
}