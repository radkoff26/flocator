package ru.flocator.feature_settings.internal.core.di

import dagger.Component
import ru.flocator.core.navigation.NavController
import ru.flocator.feature_settings.api.dependencies.SettingsDependencies
import ru.flocator.feature_settings.api.ui.SettingsFragment
import ru.flocator.feature_settings.internal.core.di.annotations.FragmentScope
import ru.flocator.feature_settings.internal.ui.fragments.BlackListFragment
import ru.flocator.feature_settings.internal.ui.fragments.ChangePasswordFragment
import ru.flocator.feature_settings.internal.ui.fragments.DeleteAccountFragment
import ru.flocator.feature_settings.internal.ui.fragments.ExitAccountFragment
import ru.flocator.feature_settings.internal.ui.fragments.PrivacySettingsFragment

@Component(
    modules = [
        SettingsModule::class
    ],
    dependencies = [
        SettingsDependencies::class,
        NavController::class
    ]
)
@FragmentScope
internal interface SettingsComponent {

    @Component.Factory
    interface Factory {
        fun create(
            dependencies: SettingsDependencies,
            navController: NavController
        ): SettingsComponent
    }

    fun inject(settingsFragment: SettingsFragment)
    fun inject(blackListFragment: BlackListFragment)
    fun inject(changePasswordFragment: ChangePasswordFragment)
    fun inject(deleteAccountFragment: DeleteAccountFragment)
    fun inject(exitAccountFragment: ExitAccountFragment)
    fun inject(privacySettingsFragment: PrivacySettingsFragment)
}