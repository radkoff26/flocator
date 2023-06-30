package ru.flocator.app

import android.content.Context
import dagger.BindsInstance
import dagger.Component
import ru.flocator.app.di.modules.app.RepositoryModule
import ru.flocator.app.di.modules.app.RestAPIModule
import javax.inject.Singleton

@Component(
    modules = [
        RepositoryModule::class,
        RestAPIModule::class
    ]
)
@Singleton
interface MainActivityComponent {

    @Component.Builder
    abstract class Builder {
        @BindsInstance
        abstract fun context(context: Context): Builder
        abstract fun build(): MainActivityComponent
    }

    fun inject(activity: MainActivity)
}
