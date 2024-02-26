package ru.flocator.app.controller

import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import ru.flocator.app.R
import ru.flocator.core.navigation.NavController
import ru.flocator.feature_auth.api.ui.AuthFragment
import ru.flocator.feature_auth.api.ui.LocationRequestFragment
import ru.flocator.feature_community.api.ui.ProfileFragment
import ru.flocator.feature_main.api.ui.MainFragment
import ru.flocator.feature_settings.api.ui.SettingsFragment

class NavControllerImpl constructor(
    private var _activity: FragmentActivity?
) : NavController, DefaultLifecycleObserver {
    @IdRes
    private val fragmentContainerId = R.id.fragment_container

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

    override fun toAuthWithBackStackCleared() {
        fragmentManager.clearBackStack(BACK_STACK_NAME)
        fragmentManager.beginTransaction().apply {
            fragmentManager.fragments.forEach {
                remove(it)
            }
            add(fragmentContainerId, AuthFragment.newInstance())
            addToBackStack(BACK_STACK_NAME)
            commit()
        }
    }

    override fun toAuth() {
        openFragment(AuthFragment.newInstance())
    }

    override fun toLocationDialog() {
        openFragment(LocationRequestFragment.newInstance())
    }

    override fun toMain() {
        openFragment(MainFragment.newInstance())
    }

    override fun toProfile() {
        openFragment(ProfileFragment.newInstance())
    }

    override fun toSettings() {
        openFragment(SettingsFragment.newInstance())
    }

    override fun toFragment(fragment: Fragment) {
        openFragment(fragment)
    }

    override fun back() {
        val fragments = fragmentManager.fragments
        val lastFragment = fragments.last()
        if (activity.supportFragmentManager.backStackEntryCount > 1
            && lastFragment !is MainFragment
            && lastFragment !is AuthFragment
            && lastFragment !is LocationRequestFragment
        ) {
            fragmentManager.popBackStack()
        } else {
            activity.finish()
        }
    }

    private fun openFragment(fragment: Fragment) {
        activity.supportFragmentManager.beginTransaction().apply {
            openOrReplaceFragment(fragment)
            addToBackStack(BACK_STACK_NAME)
            commit()
        }
    }

    private fun FragmentTransaction.openOrReplaceFragment(
        fragment: Fragment
    ) {
        if (activity.supportFragmentManager.fragments.isEmpty()) {
            add(fragmentContainerId, fragment)
        } else {
            replace(fragmentContainerId, fragment)
        }
    }

    companion object {
        private const val BACK_STACK_NAME = "back_stack"
    }
}