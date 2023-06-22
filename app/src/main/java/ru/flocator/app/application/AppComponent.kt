package ru.flocator.app.application

import android.content.Context
import dagger.BindsInstance
import dagger.Component
import ru.flocator.app.di.*

@Component(
    modules = [
        ApiModule::class,
        CacheModule::class,
        ConnectionModule::class,
        DatabaseModule::class,
        ReceiverModule::class,
        RepositoryModule::class,
        SharedStorageModule::class
    ]
)
interface AppComponent {

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance context: Context): AppComponent
    }

    fun inject(app: App)
}