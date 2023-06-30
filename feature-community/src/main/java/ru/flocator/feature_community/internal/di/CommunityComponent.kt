package ru.flocator.feature_community.internal.di

import androidx.fragment.app.Fragment
import dagger.BindsInstance
import dagger.Component
import ru.flocator.feature_community.api.ui.ProfileFragment
import ru.flocator.feature_community.internal.fragments.AddFriendByLinkFragment
import ru.flocator.feature_community.internal.fragments.OtherPersonProfileFragment

@Component(
    modules = [
        CommunityModule::class
    ],
    dependencies = [
        Fragment::class
    ]
)
internal interface CommunityComponent {

    @Component.Builder
    abstract class Builder {
        @BindsInstance
        abstract fun fragment(fragment: Fragment): Builder
        abstract fun build(): CommunityComponent
    }

    fun inject(profileFragment: ProfileFragment)
    fun inject(addFriendByLinkFragment: AddFriendByLinkFragment)
    fun inject(otherPersonProfileFragment: OtherPersonProfileFragment)
}