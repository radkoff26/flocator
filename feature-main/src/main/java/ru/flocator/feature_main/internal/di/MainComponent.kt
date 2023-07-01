package ru.flocator.feature_main.internal.di

import dagger.Component
import ru.flocator.core_controller.NavController
import ru.flocator.feature_main.api.dependencies.MainDependencies
import ru.flocator.feature_main.api.ui.MainFragment
import ru.flocator.feature_main.internal.di.annotations.FragmentScope
import ru.flocator.feature_main.internal.ui.AddMarkFragment
import ru.flocator.feature_main.internal.ui.MarkFragment
import ru.flocator.feature_main.internal.ui.MarksListFragment

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
