package ru.flocator.feature_community.internal.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import retrofit2.create
import ru.flocator.core_api.api.AppRepository
import ru.flocator.core_view_model.ViewModelFactory
import ru.flocator.core_view_model.ViewModelsMap
import ru.flocator.core_view_model.annotations.ViewModelKey
import ru.flocator.feature_community.api.dependencies.CommunityDependencies
import ru.flocator.feature_community.internal.data_source.UserAPI
import ru.flocator.feature_community.internal.di.annotations.FragmentScope
import ru.flocator.feature_community.internal.view_models.AddFriendByLinkFragmentViewModel
import ru.flocator.feature_community.internal.view_models.OtherPersonProfileFragmentViewModel
import ru.flocator.feature_community.internal.view_models.ProfileFragmentViewModel

@Module
internal abstract class CommunityModule {

    companion object {
        @Provides
        @FragmentScope
        fun provideUserAPI(dependencies: CommunityDependencies): UserAPI =
            dependencies.retrofit.create()

        @Provides
        @FragmentScope
        fun provideViewModelFactory(map: ViewModelsMap): ViewModelProvider.Factory =
            ViewModelFactory(map)
    }

    @Binds
    @FragmentScope
    @IntoMap
    @ViewModelKey(AddFriendByLinkFragmentViewModel::class)
    abstract fun bindAddFriendByLinkFragmentViewModel(impl: AddFriendByLinkFragmentViewModel): ViewModel

    @Binds
    @FragmentScope
    @IntoMap
    @ViewModelKey(OtherPersonProfileFragmentViewModel::class)
    abstract fun bindOtherPersonProfileFragmentViewModel(impl: OtherPersonProfileFragmentViewModel): ViewModel

    @Binds
    @FragmentScope
    @IntoMap
    @ViewModelKey(ProfileFragmentViewModel::class)
    abstract fun bindProfileFragmentViewModel(impl: ProfileFragmentViewModel): ViewModel
}