package ru.flocator.app.di.modules.external

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import ru.flocator.app.application.AppComponent
import ru.flocator.app.di.annotations.DependenciesKey
import ru.flocator.app.di.modules.app.ConnectionModule
import ru.flocator.app.di.modules.app.RepositoryModule
import ru.flocator.app.di.modules.app.RestAPIModule
import ru.flocator.core_dependency.Dependencies
import ru.flocator.feature_main.api.dependencies.MainDependencies
import javax.inject.Singleton

@Module(
    includes = [
        ConnectionModule::class,
        RepositoryModule::class,
        RestAPIModule::class
    ]
)
interface MainDependenciesModule {

    @Binds
    @Singleton
    @IntoMap
    @DependenciesKey(MainDependencies::class)
    fun bindsMainDependencies(impl: AppComponent): Dependencies
}