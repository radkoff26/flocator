package ru.flocator.feature_community.internal.di

import dagger.Component
import ru.flocator.core_controller.NavController
import ru.flocator.feature_community.api.dependencies.CommunityDependencies
import ru.flocator.feature_community.api.ui.ProfileFragment
import ru.flocator.feature_community.internal.di.annotations.FragmentScope
import ru.flocator.feature_community.internal.ui.AddFriendByLinkFragment
import ru.flocator.feature_community.internal.ui.OtherPersonProfileFragment

@Component(
    modules = [
        CommunityModule::class
    ],
    dependencies = [
        CommunityDependencies::class,
        NavController::class
    ]
)
@FragmentScope
internal interface CommunityComponent {

    @Component.Builder
    abstract class Builder {
        abstract fun communityDependencies(dependencies: CommunityDependencies): Builder
        abstract fun navController(navController: NavController): Builder
        abstract fun build(): CommunityComponent
    }

    fun inject(profileFragment: ProfileFragment)
    fun inject(addFriendByLinkFragment: AddFriendByLinkFragment)
    fun inject(otherPersonProfileFragment: OtherPersonProfileFragment)
}