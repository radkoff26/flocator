package ru.flocator.app.di.modules.external

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import ru.flocator.app.application.AppComponent
import ru.flocator.app.di.modules.app.ConnectionModule
import ru.flocator.core_dependency.Dependencies
import ru.flocator.app.di.annotations.DependenciesKey
import ru.flocator.feature_main.api.dependencies.MainDeps
import javax.inject.Singleton

@Module(
    includes = [
        ConnectionModule::class
    ]
)
abstract class MainDepsModule {

    @Binds
    @Singleton
    @IntoMap
    @DependenciesKey(MainDeps::class)
    abstract fun bindsMainDeps(impl: AppComponent): Dependencies
}