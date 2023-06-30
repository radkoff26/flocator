package ru.flocator.feature_settings.internal.di

import androidx.fragment.app.Fragment
import dagger.Component
import ru.flocator.feature_settings.api.ui.SettingsFragment
import ru.flocator.feature_settings.internal.ui.*
import ru.flocator.feature_settings.internal.ui.BlackListFragment
import ru.flocator.feature_settings.internal.ui.ChangePasswordFragment
import ru.flocator.feature_settings.internal.ui.DeleteAccountFragment
import ru.flocator.feature_settings.internal.ui.ExitAccountFragment

@Component(
    modules = [
        SettingsModule::class
    ],
    dependencies = [
        Fragment::class
    ]
)
internal interface SettingsComponent {

    @Component.Factory
    interface Factory {
        fun create(fragment: Fragment): SettingsComponent
    }

    fun inject(settingsFragment: SettingsFragment)
    fun inject(blackListFragment: BlackListFragment)
    fun inject(changePasswordFragment: ChangePasswordFragment)
    fun inject(deleteAccountFragment: DeleteAccountFragment)
    fun inject(exitAccountFragment: ExitAccountFragment)
    fun inject(privacySettingsFragment: PrivacySettingsFragment)
}