package ru.flocator.app.di.modules.external

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import ru.flocator.app.application.AppComponent
import ru.flocator.app.di.annotations.DependenciesKey
import ru.flocator.app.di.modules.app.RepositoryModule
import ru.flocator.app.di.modules.app.RestAPIModule
import ru.flocator.core_dependency.Dependencies
import ru.flocator.feature_auth.api.dependencies.AuthDependencies
import javax.inject.Singleton

@Module(
    includes = [
        RestAPIModule::class,
        RepositoryModule::class
    ]
)
interface AuthDependenciesModule {

    @Binds
    @Singleton
    @IntoMap
    @DependenciesKey(AuthDependencies::class)
    fun bindAuthDependencies(impl: AppComponent): Dependencies
}