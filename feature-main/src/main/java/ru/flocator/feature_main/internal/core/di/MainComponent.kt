package ru.flocator.feature_main.internal.core.di

import dagger.Component
import ru.flocator.core.navigation.NavController
import ru.flocator.feature_main.api.dependencies.MainDependencies
import ru.flocator.feature_main.api.ui.MainFragment
import ru.flocator.feature_main.internal.core.di.annotations.FragmentScope
import ru.flocator.feature_main.internal.ui.fragments.AddMarkFragment
import ru.flocator.feature_main.internal.ui.fragments.MarkFragment
import ru.flocator.feature_main.internal.ui.fragments.MarksListFragment

@Component(
    modules = [
        MainModule::class
    ],
    dependencies = [
        MainDependencies::class,
        NavController::class
    ]
)
@FragmentScope
internal interface MainComponent {

    @Component.Builder
    abstract class Builder {
        abstract fun mainDependencies(dependencies: MainDependencies): Builder
        abstract fun navController(navController: NavController): Builder
        abstract fun build(): MainComponent
    }

    fun inject(addMarkFragment: AddMarkFragment)
    fun inject(mainFragment: MainFragment)
    fun inject(markFragment: MarkFragment)
    fun inject(marksListFragment: MarksListFragment)
}
