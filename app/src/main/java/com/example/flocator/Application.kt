package com.example.flocator

import com.example.flocator.common.config.Constants.MAPS_API_KEY
import com.example.flocator.community.fragments.PersonRepository
import com.yandex.mapkit.MapKitFactory

class Application : android.app.Application() {
    val personService by lazy { PersonRepository() }

    override fun onCreate() {
        super.onCreate()
        MapKitFactory.setApiKey(MAPS_API_KEY)
    }
}