package ru.flocator.feature_settings.internal.di

import dagger.Component
import ru.flocator.core_controller.NavController
import ru.flocator.feature_settings.api.dependencies.SettingsDependencies
import ru.flocator.feature_settings.api.ui.SettingsFragment
import ru.flocator.feature_settings.internal.di.annotations.FragmentScope
import ru.flocator.feature_settings.internal.ui.*

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