package ru.flocator.app

import android.content.IntentFilter
import android.util.Log
import ru.flocator.core_config.Actions
import ru.flocator.core_receivers.NetworkReceiver
import dagger.hilt.android.HiltAndroidApp
import io.reactivex.exceptions.UndeliverableException
import io.reactivex.plugins.RxJavaPlugins
import javax.inject.Inject

@HiltAndroidApp
class Application : android.app.Application() {
    @Inject
    lateinit var networkReceiver: NetworkReceiver

    override fun onCreate() {
        super.onCreate()
        RxJavaPlugins.setErrorHandler {
            if (it is UndeliverableException) {
                Log.i(TAG, "Flow was disposed before finishing its work!", it)
            }
        }
        registerReceiver(
            networkReceiver, IntentFilter(Actions.CONNECTIVITY_CHANGE)
        )
    }

    companion object {
        private const val TAG = "Application Class"
    }
}