package ru.flocator.app.application

import android.content.Context
import dagger.BindsInstance
import dagger.Component
import ru.flocator.app.MainActivity
import ru.flocator.app.di.modules.app.DataStoreModule
import ru.flocator.app.di.modules.app.ReceiverModule
import ru.flocator.app.di.modules.app.RestAPIModule
import ru.flocator.app.di.modules.external.AuthDependenciesModule
import ru.flocator.app.di.modules.external.CommunityDependenciesModule
import ru.flocator.app.di.modules.external.MainDependenciesModule
import ru.flocator.app.di.modules.external.SettingsDependenciesModule
import ru.flocator.feature_auth.api.dependencies.AuthDependencies
import ru.flocator.feature_community.api.dependencies.CommunityDependencies
import ru.flocator.feature_main.api.dependencies.MainDependencies
import ru.flocator.feature_settings.api.dependencies.SettingsDependencies
import javax.inject.Singleton

@Component(
    modules = [
        DataStoreModule::class,
        RestAPIModule::class,
        ReceiverModule::class,
        MainDependenciesModule::class,
        AuthDependenciesModule::class,
        CommunityDependenciesModule::class,
        SettingsDependenciesModule::class
    ]
)
@Singleton
interface AppComponent :
    AuthDependencies,
    CommunityDependencies,
    MainDependencies,
    SettingsDependencies {

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance context: Context): AppComponent
    }

    fun inject(app: App)
    fun inject(activity: MainActivity)
}