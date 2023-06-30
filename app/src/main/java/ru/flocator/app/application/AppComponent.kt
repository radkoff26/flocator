package ru.flocator.app.application

import android.content.Context
import dagger.BindsInstance
import dagger.Component
import ru.flocator.app.di.modules.app.ReceiverModule
import ru.flocator.app.di.modules.external.AuthDependenciesModule
import ru.flocator.app.di.modules.external.CommunityDependenciesModule
import ru.flocator.app.di.modules.external.MainDependenciesModule
import ru.flocator.feature_auth.api.dependencies.AuthDependencies
import ru.flocator.feature_community.api.dependencies.CommunityDependencies
import ru.flocator.feature_main.api.dependencies.MainDependencies
import javax.inject.Singleton

@Component(
    modules = [
        ReceiverModule::class,
        MainDependenciesModule::class,
        AuthDependenciesModule::class,
        CommunityDependenciesModule::class
    ]
)
@Singleton
interface AppComponent : MainDependencies, AuthDependencies, CommunityDependencies {

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance context: Context): AppComponent
    }

    fun inject(app: App)
}