package ru.flocator.app.application

import android.app.Application
import android.content.IntentFilter
import android.util.Log
import io.reactivex.exceptions.UndeliverableException
import io.reactivex.plugins.RxJavaPlugins
import ru.flocator.core_config.Actions
import ru.flocator.core_dependency.DependenciesContainer
import ru.flocator.core_dependency.DependenciesMap
import ru.flocator.core_receivers.NetworkReceiver
import javax.inject.Inject

class App : Application(), DependenciesContainer {

    @Inject
    override lateinit var dependenciesMap: DependenciesMap

    @Inject
    lateinit var networkReceiver: NetworkReceiver

    override fun onCreate() {
        DaggerAppComponent.factory()
            .create(this)
            .inject(this)
        super.onCreate()
        RxJavaPlugins.setErrorHandler {
            if (it is UndeliverableException) {
                Log.i(TAG, "Flow was disposed before finishing its work!", it)
            }
        }
        registerReceiver(networkReceiver, IntentFilter(Actions.CONNECTIVITY_CHANGE))
    }

    companion object {
        private const val TAG = "Application Class"
    }
}