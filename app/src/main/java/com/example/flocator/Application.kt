package com.example.flocator

import com.example.flocator.community.fragments.PersonRepository
import com.yandex.mapkit.MapKitFactory

class Application : android.app.Application() {
    companion object {
        const val API_KEY = "fd3ecdab-a39c-4b8b-a215-44a8458a84bf"
    }

    val personService by lazy { PersonRepository() }

    override fun onCreate() {
        super.onCreate()
        MapKitFactory.setApiKey(API_KEY)
    }
}