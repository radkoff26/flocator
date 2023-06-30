package ru.flocator.feature_auth.internal.di

import dagger.Module
import dagger.Provides
import retrofit2.create
import ru.flocator.feature_auth.api.dependencies.AuthDependencies
import ru.flocator.feature_auth.internal.data_source.AuthAPI
import ru.flocator.feature_auth.internal.di.annotations.FragmentScope

@Module
internal object AuthModule {

    @Provides
    @FragmentScope
    fun provideAuthAPI(dependencies: AuthDependencies): AuthAPI =
        dependencies.retrofit.create()
}
