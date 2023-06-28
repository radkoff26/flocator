package ru.flocator.app

import dagger.Component
import ru.flocator.app.di.modules.app.RepositoryModule

@Component(modules = [RepositoryModule::class])
interface MainActivityComponent {

    fun inject(activity: MainActivity)
}
