package ru.flocator.feature_auth.internal.core.di

import dagger.Component
import ru.flocator.core.navigation.NavController
import ru.flocator.feature_auth.api.dependencies.AuthDependencies
import ru.flocator.feature_auth.api.ui.AuthFragment
import ru.flocator.feature_auth.api.ui.LocationRequestFragment
import ru.flocator.feature_auth.internal.core.di.annotations.FragmentScope
import ru.flocator.feature_auth.internal.ui.fragments.RegFirstFragment
import ru.flocator.feature_auth.internal.ui.fragments.RegSecondFragment
import ru.flocator.feature_auth.internal.ui.fragments.RegThirdFragment

@Component(
    modules = [
        AuthModule::class
    ],
    dependencies = [
        AuthDependencies::class,
        NavController::class
    ]
)
@FragmentScope
internal interface AuthComponent {

    @Component.Factory
    interface Factory {
        fun create(dependencies: AuthDependencies, controller: NavController): AuthComponent
    }

    fun inject(authFragment: AuthFragment)
    fun inject(locationRequestFragment: LocationRequestFragment)
    fun inject(regFirstFragment: RegFirstFragment)
    fun inject(regSecondFragment: RegSecondFragment)
    fun inject(regThirdFragment: RegThirdFragment)
}