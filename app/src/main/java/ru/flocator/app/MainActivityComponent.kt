package ru.flocator.app

import dagger.Component
import ru.flocator.app.di.RepositoryModule
import ru.flocator.core_api.api.MainRepository

@Component(modules = [RepositoryModule::class])
interface MainActivityComponent {

    fun inject(activity: MainActivity)
}
