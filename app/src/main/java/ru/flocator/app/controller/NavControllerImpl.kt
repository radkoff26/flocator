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
            add(R.id.fragment_container, fragment)
            addToBackStack(null)
            commit()
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
        val fragments = fragmentManager.fragments
        val transaction = fragmentManager.beginTransaction()
        fragments.forEach {
            transaction.remove(it)
        }
        return transaction
    }

    private fun closeSection(): FragmentTransaction {
        val fragments = fragmentManager.fragments.reversed()
        val currentSection = getFragmentSection(fragments[0])
        val transaction = fragmentManager.beginTransaction()
        fragments.forEach {
            if (getFragmentSection(it) == currentSection) {
                transaction.remove(it)
            } else {
                return@forEach
            }
        }
        return transaction
    }

    private fun getFragmentSection(fragment: Fragment): Class<*>? {
        return when (fragment) {
            is SettingsSection -> {
                SettingsSection::class.java
            }
            is MainSection -> {
                MainSection::class.java
            }
            is AuthenticationSection -> {
                AuthenticationSection::class.java
            }
            is CommunitySection -> {
                CommunitySection::class.java
            }
            else -> null
        }
    }

    companion object {
        private const val BACK_STACK_NAME = "BACK_STACK"
    }
}