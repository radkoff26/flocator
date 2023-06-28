package ru.flocator.feature_main.internal.main.di

import dagger.Component
import dagger.android.AndroidInjector
import ru.flocator.feature_main.api.dependencies.MainDeps
import ru.flocator.feature_main.api.ui.MainFragment

@Component(dependencies = [MainDeps::class])
internal interface MainFragmentComponent: AndroidInjector<MainFragment> {

    @Component.Factory
    interface Factory {
        fun create(mainDeps: MainDeps): MainFragmentComponent
    }
}