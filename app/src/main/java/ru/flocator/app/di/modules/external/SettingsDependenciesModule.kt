package ru.flocator.app.di.modules.external

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import ru.flocator.app.application.AppComponent
import ru.flocator.app.di.annotations.DependenciesKey
import ru.flocator.app.di.modules.app.*
import ru.flocator.core.dependencies.Dependencies
import ru.flocator.feature_settings.api.dependencies.SettingsDependencies
import javax.inject.Singleton

@Module(
    includes = [
        ConnectionModule::class,
        DataStoreModule::class,
        RestAPIModule::class,
        StorageModule::class,
        PhotoCacheModule::class
    ]
)
interface SettingsDependenciesModule {

    @Binds
    @Singleton
    @IntoMap
    @DependenciesKey(SettingsDependencies::class)
    fun bindSettingsDependencies(impl: AppComponent): Dependencies
}