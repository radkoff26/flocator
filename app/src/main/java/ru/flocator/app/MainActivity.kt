package ru.flocator.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.FragmentContainerView
import ru.flocator.app.application.App
import ru.flocator.app.controller.NavControllerImpl
import ru.flocator.core.alert.ErrorDebouncingAlertPoller
import ru.flocator.core.alert.OnDismissedCallback
import ru.flocator.core.alert.OnErrorCallback
import ru.flocator.core.base.activity.BaseActivity
import ru.flocator.core.navigation.NavController
import ru.flocator.core.navigation.NavigationRoot
import ru.flocator.core.utils.LocationUtils
import ru.flocator.data.models.language.Language
import ru.flocator.data.preferences.LanguagePreferences
import ru.flocator.design.SnackbarComposer
import java.util.*

class MainActivity : BaseActivity(), NavigationRoot {
    override fun composeSnackbar(view: View, text: String, onDismissed: () -> Unit) {
        SnackbarComposer.composeDesignedSnackbar(view, text, onDismissed)
    }

    private var fragmentContainer: FragmentContainerView? = null

    private val networkBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent ?: return
            intent.action ?: return
            when (intent.action) {
                Broadcasts.CONNECTION_FAILED -> {
                    fragmentContainer?.let {
                        notifyAboutError(getString(R.string.connection_lost), it)
                    }
                }
                Broadcasts.AUTHORIZATION_FAILED -> {
                    navController.toAuthWithBackStackCleared()
                }
            }
        }
    }

    override lateinit var navController: NavController

    override fun attachBaseContext(newBase: Context?) {
        // If context is not null
        if (newBase != null) {
            // Then it's possible to check if some language preference is saved
            val language = LanguagePreferences(newBase).getLanguage()
            // If language is set
            if (language != null) {
                // Then it's set to context
                super.attachBaseContext(createContextWithLanguage(newBase, language))
            } else {
                // Otherwise, default context takes place
                super.attachBaseContext(newBase)
            }
        } else {
            // Otherwise, it's not possible
            super.attachBaseContext(null)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as App).appComponent.inject(this)
        navController = NavControllerImpl(this)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()

        fragmentContainer = findViewById(R.id.fragment_container)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navController.back()
            }
        })

        if (supportFragmentManager.fragments.isEmpty()) {
            openFirstFragment()
        }

        registerReceiver(
            networkBroadcastReceiver,
            IntentFilter().apply {
                addAction(Broadcasts.CONNECTION_FAILED)
                addAction(Broadcasts.AUTHORIZATION_FAILED)
            }
        )
    }

    override fun onDestroy() {
        unregisterReceiver(networkBroadcastReceiver)
        super.onDestroy()
    }

    private fun createContextWithLanguage(context: Context, language: Language): Context {
        val locale = Locale(
            language.toString().lowercase()
        )
        val configuration: Configuration = context.resources.configuration
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)
        return context.createConfigurationContext(configuration)
    }

    private fun openFirstFragment() {
        if (LocationUtils.hasLocationPermission(this)) {
            navController.toMain()
        } else {
            navController.toLocationDialog()
        }
    }
}