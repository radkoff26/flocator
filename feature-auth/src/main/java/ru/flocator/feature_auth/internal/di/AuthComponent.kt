package ru.flocator.feature_auth.internal.di

import dagger.Component
import ru.flocator.core_controller.NavController
import ru.flocator.feature_auth.api.dependencies.AuthDependencies
import ru.flocator.feature_auth.api.ui.AuthFragment
import ru.flocator.feature_auth.internal.di.annotations.FragmentScope
import ru.flocator.feature_auth.internal.ui.RegFirstFragment
import ru.flocator.feature_auth.internal.ui.RegSecondFragment
import ru.flocator.feature_auth.internal.ui.RegThirdFragment

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
    fun inject(regFirstFragment: RegFirstFragment)
    fun inject(regSecondFragment: RegSecondFragment)
    fun inject(regThirdFragment: RegThirdFragment)
}