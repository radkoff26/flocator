package ru.flocator.app.application

import android.content.Context
import dagger.BindsInstance
import dagger.Component
import ru.flocator.app.di.modules.app.ReceiverModule
import ru.flocator.app.di.modules.external.MainDepsModule
import ru.flocator.feature_main.api.dependencies.MainDeps
import javax.inject.Singleton

@Component(
    modules = [
        ReceiverModule::class,
        MainDepsModule::class
    ]
)
@Singleton
interface AppComponent : MainDeps {

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance context: Context): AppComponent
    }

    fun inject(app: App)
}