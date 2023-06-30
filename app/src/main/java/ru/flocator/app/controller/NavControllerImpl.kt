package ru.flocator.app.controller

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import ru.flocator.app.R
import ru.flocator.core_controller.NavController
import ru.flocator.core_controller.TransactionCommitter
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

    private val fragmentManager: FragmentManager = activity.supportFragmentManager

    init {
        activity.lifecycle.addObserver(this)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        activity.lifecycle.removeObserver(this)
        _activity = null
    }

    override fun toAuth(): TransactionCommitter =
        object : TransactionCommitter() {
            override fun commit() {
                openFragment(AuthFragment(), this)
            }
        }

    override fun toLocationDialog(): TransactionCommitter =
        object : TransactionCommitter() {
            override fun commit() {
                openFragment(LocationRequestFragment(), this)
            }
        }

    override fun toMain(): TransactionCommitter =
        object : TransactionCommitter() {
            override fun commit() {
                openFragment(MainFragment(), this)
            }
        }

    override fun toProfile(): TransactionCommitter =
        object : TransactionCommitter() {
            override fun commit() {
                openFragment(ProfileFragment(), this)
            }
        }

    override fun toSettings(): TransactionCommitter =
        object : TransactionCommitter() {
            override fun commit() {
                openFragment(SettingsFragment(), this)
            }
        }

    override fun toFragment(fragment: Fragment): TransactionCommitter =
        object : TransactionCommitter() {
            override fun commit() {
                openFragment(fragment, this)
            }
        }

    override fun back() {
        if (fragmentManager.fragments.size > 1) {
            fragmentManager.popBackStack()
        } else {
            activity.finish()
        }
    }

    private fun openFragment(fragment: Fragment, committer: TransactionCommitter) {
        processTransactionCommitterSettings(committer).apply {
            replace(R.id.fragment_container, fragment)
            addToBackStack(null)
            commitNow()
            fragmentManager.saveBackStack(BACK_STACK_NAME)
        }
    }

    private fun processTransactionCommitterSettings(committer: TransactionCommitter): FragmentTransaction =
        if (committer.clearAll) {
            clearAllFragments()
        } else if (committer.closeSection) {
            closeSection()
        } else {
            fragmentManager.beginTransaction()
        }

    private fun clearAllFragments(): FragmentTransaction {
        fragmentManager.clearBackStack(BACK_STACK_NAME)
        return fragmentManager.beginTransaction()
    }

    private fun closeSection(): FragmentTransaction {
//        val fragments = fragmentManager.fragments.reversed()
//        val currentSection = fragments[0]
//        val transaction = fragmentManager.beginTransaction()
//        fragments.forEach {
//            if (currentSection::class.java.isInstance(it)) {
//                transaction.remove(it)
//            } else {
//                return@forEach
//            }
//        }
//        return transaction
        val fragments = fragmentManager.fragments
        val lastFragment = fragments.last()!!
        val transaction = fragmentManager.beginTransaction()
        val currentSectionClass = getFragmentSection(lastFragment) ?: return transaction
        while (fragmentManager.fragments.size > 0 && currentSectionClass::class.java.isInstance(
                fragmentManager.fragments.last()
            )
        ) {
            fragmentManager.popBackStack()
        }
        return transaction
    }

    private fun getFragmentSection(fragment: Fragment): Class<*>? =
        if (MainSection::class.java.isInstance(fragment)) {
            MainSection::class.java
        } else if (CommunitySection::class.java.isInstance(fragment)) {
            CommunitySection::class.java
        } else if (AuthenticationSection::class.java.isInstance(fragment)) {
            AuthenticationSection::class.java
        } else if (SettingsSection::class.java.isInstance(fragment)) {
            SettingsSection::class.java
        } else {
            null
        }

    companion object {
        private const val BACK_STACK_NAME = "BACK_STACK"
    }
}