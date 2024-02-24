package ru.flocator.app.di.modules.external

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import ru.flocator.app.application.AppComponent
import ru.flocator.app.di.annotations.DependenciesKey
import ru.flocator.app.di.modules.app.*
import ru.flocator.core.dependencies.Dependencies
import ru.flocator.feature_main.api.dependencies.MainDependencies
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
interface MainDependenciesModule {

    @Binds
    @Singleton
    @IntoMap
    @DependenciesKey(MainDependencies::class)
    fun bindsMainDependencies(impl: AppComponent): Dependencies
}