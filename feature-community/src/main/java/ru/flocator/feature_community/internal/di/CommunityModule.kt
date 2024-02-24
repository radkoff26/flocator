package ru.flocator.feature_community.internal.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import retrofit2.create
import ru.flocator.core.view_model.ViewModelFactory
import ru.flocator.core.view_model.ViewModelsMap
import ru.flocator.core.view_model.annotations.ViewModelKey
import ru.flocator.feature_community.api.dependencies.CommunityDependencies
import ru.flocator.feature_community.internal.data_source.UserDataSource
import ru.flocator.feature_community.internal.di.annotations.FragmentScope
import ru.flocator.feature_community.internal.view_models.AddFriendByLinkViewModel
import ru.flocator.feature_community.internal.view_models.ExternalProfileViewModel
import ru.flocator.feature_community.internal.view_models.ProfileViewModel

@Module
internal abstract class CommunityModule {

    companion object {
        @Provides
        @FragmentScope
        fun provideUserDataSource(dependencies: CommunityDependencies): UserDataSource =
            dependencies.retrofit.create()

        @Provides
        @FragmentScope
        fun provideViewModelFactory(map: ViewModelsMap): ViewModelProvider.Factory =
            ViewModelFactory(map)
    }

    @Binds
    @FragmentScope
    @IntoMap
    @ViewModelKey(AddFriendByLinkViewModel::class)
    abstract fun bindAddFriendByLinkFragmentViewModel(impl: AddFriendByLinkViewModel): ViewModel

    @Binds
    @FragmentScope
    @IntoMap
    @ViewModelKey(ExternalProfileViewModel::class)
    abstract fun bindOtherPersonProfileFragmentViewModel(impl: ExternalProfileViewModel): ViewModel

    @Binds
    @FragmentScope
    @IntoMap
    @ViewModelKey(ProfileViewModel::class)
    abstract fun bindProfileFragmentViewModel(impl: ProfileViewModel): ViewModel
}