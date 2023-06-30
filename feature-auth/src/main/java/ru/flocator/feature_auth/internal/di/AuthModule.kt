package ru.flocator.feature_auth.internal.di

import androidx.fragment.app.Fragment
import dagger.Module
import dagger.Provides
import retrofit2.create
import ru.flocator.core_controller.NavController
import ru.flocator.core_controller.findNavController
import ru.flocator.core_dependency.findDependencies
import ru.flocator.feature_auth.api.dependencies.AuthDependencies
import ru.flocator.feature_auth.internal.data_source.AuthAPI
import ru.flocator.feature_auth.internal.di.annotations.FragmentScope

@Module
internal class AuthModule {

    @Provides
    @FragmentScope
    fun provideNavController(fragment: Fragment): NavController =
        fragment.findNavController()

    @Provides
    @FragmentScope
    fun provideAuthDependencies(fragment: Fragment): AuthDependencies =
        fragment.findDependencies()

    @Provides
    @FragmentScope
    fun provideAuthAPI(dependencies: AuthDependencies): AuthAPI =
        dependencies.retrofit.create()
}
