package ru.flocator.feature_community.internal.di

import dagger.Module
import dagger.Provides
import retrofit2.create
import ru.flocator.core_api.api.AppRepository
import ru.flocator.feature_community.api.dependencies.CommunityDependencies
import ru.flocator.feature_community.internal.data_source.UserAPI
import ru.flocator.feature_community.internal.di.annotations.FragmentScope

@Module
internal object CommunityModule {

    @Provides
    @FragmentScope
    fun provideUserAPI(dependencies: CommunityDependencies): UserAPI =
        dependencies.retrofit.create()
}