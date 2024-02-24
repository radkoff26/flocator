package ru.flocator.app.di.modules.external

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import ru.flocator.app.application.AppComponent
import ru.flocator.app.di.annotations.DependenciesKey
import ru.flocator.app.di.modules.app.DataStoreModule
import ru.flocator.app.di.modules.app.RestAPIModule
import ru.flocator.core.dependencies.Dependencies
import ru.flocator.feature_auth.api.dependencies.AuthDependencies
import javax.inject.Singleton

@Module(
    includes = [
        DataStoreModule::class,
        RestAPIModule::class
    ]
)
interface AuthDependenciesModule {

    @Binds
    @Singleton
    @IntoMap
    @DependenciesKey(AuthDependencies::class)
    fun bindAuthDependencies(impl: AppComponent): Dependencies
}