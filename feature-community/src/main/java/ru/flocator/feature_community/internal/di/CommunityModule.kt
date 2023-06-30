package ru.flocator.feature_community.internal.di

import androidx.fragment.app.Fragment
import dagger.Module
import dagger.Provides
import retrofit2.create
import ru.flocator.core_api.api.AppRepository
import ru.flocator.core_controller.NavController
import ru.flocator.core_controller.findNavController
import ru.flocator.core_dependency.findDependencies
import ru.flocator.feature_community.api.dependencies.CommunityDependencies
import ru.flocator.feature_community.internal.data_source.UserAPI
import ru.flocator.feature_community.internal.di.annotations.FragmentScope

@Module
internal object CommunityModule {

    @Provides
    @FragmentScope
    fun provideNavController(fragment: Fragment): NavController =
        fragment.findNavController()

    @Provides
    @FragmentScope
    fun provideCommunityDependencies(fragment: Fragment): CommunityDependencies =
        fragment.findDependencies()

    @Provides
    @FragmentScope
    fun provideAppRepository(dependencies: CommunityDependencies): AppRepository =
        dependencies.appRepository

    @Provides
    @FragmentScope
    fun provideUserAPI(dependencies: CommunityDependencies): UserAPI =
        dependencies.retrofit.create()
}