package ru.flocator.feature_auth.internal.di

import androidx.fragment.app.Fragment
import dagger.Component
import dagger.android.AndroidInjector
import ru.flocator.feature_auth.api.ui.AuthFragment
import ru.flocator.feature_auth.internal.di.annotations.FragmentScope

@Component(
    modules = [
        AuthModule::class
    ],
    dependencies = [
        Fragment::class
    ]
)
@FragmentScope
interface AuthComponent : AndroidInjector<AuthFragment> {

    @Component.Factory
    interface Factory {
        fun create(fragment: Fragment): AuthComponent
    }
}