package com.example.flocator.common.utils

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.example.flocator.R
import com.example.flocator.authentication.Authentication
import com.example.flocator.community.CommunitySection
import com.example.flocator.main.MainSection
import com.example.flocator.settings.SettingsSection

object FragmentNavigationUtils {

    fun openFragment(fragmentManager: FragmentManager, fragment: Fragment) {
        openFragment(fragmentManager, fragment, true)
    }

    fun openFragmentExcludingMain(fragmentManager: FragmentManager, fragment: Fragment) {
        openFragment(fragmentManager, fragment, false)
    }

    private fun openFragment(fragmentManager: FragmentManager, fragment: Fragment, excludeMain: Boolean) {
        val fragments = fragmentManager.fragments
        val transaction = fragmentManager.beginTransaction()
        if (fragments.size > 0) {
            val lastFragmentInterface = getAssignableFromInterface(fragments.lastOrNull()!!)
            val currentFragmentInterface = getAssignableFromInterface(fragment)
            if (lastFragmentInterface != null && currentFragmentInterface != null && (excludeMain && lastFragmentInterface != MainSection::class.java) && lastFragmentInterface != currentFragmentInterface) {
                transaction.apply {
                    fragments.forEach {
                        val current = getAssignableFromInterface(it)
                        if (current != null && current == lastFragmentInterface) {
                            remove(it)
                        }
                    }
                }
            }
        }
        transaction.apply {
            add(R.id.fragment_container, fragment)
            setReorderingAllowed(true)
            addToBackStack(null)
            commit()
        }
    }

    fun closeLastFragment(fragmentManager: FragmentManager, activity: FragmentActivity) {
        val fragments = fragmentManager.fragments
        val size = fragmentManager.fragments.size
        if (size < 2) {
            activity.finish()
        } else {
            val lastFragmentIndex = fragments.indexOfLast { getAssignableFromInterface(it) != null }
            if (lastFragmentIndex == -1 || getAssignableFromInterface(fragments[lastFragmentIndex]) == MainSection::class.java) {
                activity.finish()
                return
            }
            fragmentManager.beginTransaction().apply {
                remove(fragments[lastFragmentIndex])
                setReorderingAllowed(true)
                addToBackStack(null)
                commit()
            }
        }
    }

    private fun getAssignableFromInterface(fragment: Fragment): Class<*>? {
        return when (fragment) {
            is SettingsSection -> {
                SettingsSection::class.java
            }
            is MainSection -> {
                MainSection::class.java
            }
            is Authentication -> {
                Authentication::class.java
            }
            is CommunitySection -> {
                CommunitySection::class.java
            }
            else -> null
        }
    }

    fun clearAllAndOpenFragment(fragmentManager: FragmentManager, fragment: Fragment) {
        val fragments = fragmentManager.fragments
        fragmentManager.beginTransaction().apply {
            for (current in fragments) {
                remove(current)
            }
            add(R.id.fragment_container, fragment)
            setReorderingAllowed(true)
            addToBackStack(null)
            commit()
        }
    }
}