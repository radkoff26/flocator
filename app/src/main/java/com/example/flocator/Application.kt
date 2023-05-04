package com.example.flocator

import android.util.Log
import com.example.flocator.common.config.Constants.MAPS_API_KEY
import com.example.flocator.community.fragments.PersonRepository
import com.yandex.mapkit.MapKitFactory
import dagger.hilt.android.HiltAndroidApp
import io.reactivex.exceptions.UndeliverableException
import io.reactivex.plugins.RxJavaPlugins

@HiltAndroidApp
class Application : android.app.Application() {
    val personService by lazy { PersonRepository() }

    override fun onCreate() {
        super.onCreate()
        MapKitFactory.setApiKey(MAPS_API_KEY)
        RxJavaPlugins.setErrorHandler {
            if (it is UndeliverableException) {
                Log.i(TAG, "Flow was disposed before finishing its work!", it)
            }
        }
    }

    companion object {
        private const val TAG = "Application Class"
    }
}