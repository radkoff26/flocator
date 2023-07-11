package ru.flocator.app.controller

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import ru.flocator.app.R
import ru.flocator.core_controller.NavController
import ru.flocator.core_sections.AuthenticationSection
import ru.flocator.core_sections.CommunitySection
import ru.flocator.core_sections.MainSection
import ru.flocator.core_sections.SettingsSection
import ru.flocator.feature_auth.api.ui.AuthFragment
import ru.flocator.feature_auth.api.ui.LocationRequestFragment
import ru.flocator.feature_community.api.ui.ProfileFragment
import ru.flocator.feature_main.api.ui.MainFragment
import ru.flocator.feature_settings.api.ui.SettingsFragment

class NavControllerImpl constructor(private var _activity: FragmentActivity?) :
    NavController,
    DefaultLifecycleObserver {

    private val activity: FragmentActivity
        get() = _activity!!

    private var _fragmentManager: FragmentManager? = activity.supportFragmentManager
    private val fragmentManager: FragmentManager
        get() = _fragmentManager!!

    init {
        activity.lifecycle.addObserver(this)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        activity.lifecycle.removeObserver(this)
        _fragmentManager = null
        _activity = null
    }

    override fun toAuth() {
        openFragment(AuthFragment())
    }

    override fun toLocationDialog() {
        openFragment(LocationRequestFragment())
    }

    override fun toMain() {
        openFragment(MainFragment())
    }

    override fun toProfile() {
        openFragment(ProfileFragment())
    }

    override fun toSettings() {
        openFragment(SettingsFragment())
    }

    override fun toFragment(fragment: Fragment) {
        openFragment(fragment)
    }

    override fun back() {
        val fragments = fragmentManager.fragments
        val lastFragment = fragments.last()
        if (activity.supportFragmentManager.backStackEntryCount > 1
            && lastFragment !is MainFragment
            && lastFragment !is LocationRequestFragment) {
            fragmentManager.popBackStack()
        } else {
            activity.finish()
        }
    }

    private fun openFragment(fragment: Fragment) {
        activity.supportFragmentManager.beginTransaction().apply {
            openOrReplaceFragment(fragment, this)
            addToBackStack(null)
            commit()
        }
    }

    private fun openOrReplaceFragment(
        fragment: Fragment,
        fragmentTransaction: FragmentTransaction
    ) {
        if (activity.supportFragmentManager.fragments.isEmpty()) {
            fragmentTransaction.add(R.id.fragment_container, fragment)
        } else {
            fragmentTransaction.replace(R.id.fragment_container, fragment)
        }
    }
}