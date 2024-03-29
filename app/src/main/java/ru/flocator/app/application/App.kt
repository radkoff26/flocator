package ru.flocator.app.application

import android.app.Application
import android.util.Log
import io.reactivex.exceptions.UndeliverableException
import io.reactivex.plugins.RxJavaPlugins
import ru.flocator.core.dependencies.DependenciesContainer
import ru.flocator.core.dependencies.DependenciesMap
import javax.inject.Inject

class App : Application(), DependenciesContainer {

    @Inject
    override lateinit var dependenciesMap: DependenciesMap

    lateinit var appComponent: AppComponent

    override fun onCreate() {
        appComponent = DaggerAppComponent.factory()
            .create(this)
        appComponent.inject(this)
        super.onCreate()
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