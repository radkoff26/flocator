package ru.flocator.feature_auth.internal.di

import androidx.fragment.app.Fragment
import dagger.BindsInstance
import dagger.Component
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
        Fragment::class
    ]
)
@FragmentScope
internal interface AuthComponent {

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance fragment: Fragment): AuthComponent
    }

    fun inject(authFragment: AuthFragment)
    fun inject(regFirstFragment: RegFirstFragment)
    fun inject(regSecondFragment: RegSecondFragment)
    fun inject(regThirdFragment: RegThirdFragment)
}