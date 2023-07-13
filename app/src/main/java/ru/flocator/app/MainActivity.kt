package ru.flocator.app

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import ru.flocator.app.application.App
import ru.flocator.app.controller.NavControllerImpl
import ru.flocator.app.data_source.MainAPI
import ru.flocator.cache.storage.SettingsStorageImpl
import ru.flocator.cache.storage.domain.Language
import ru.flocator.core_api.api.AppRepository
import ru.flocator.core_controller.NavController
import ru.flocator.core_controller.NavigationRoot
import ru.flocator.core_dto.auth.UserCredentialsDto
import ru.flocator.core_utils.LocationUtils
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.*
import javax.inject.Inject

class MainActivity : AppCompatActivity(), NavigationRoot {

    private val compositeDisposable = CompositeDisposable()

    @Inject
    lateinit var repository: AppRepository

    @Inject
    lateinit var mainAPI: MainAPI

    override lateinit var navController: NavController

    override fun attachBaseContext(newBase: Context?) {
        // If context is not null
        if (newBase != null) {
            // Then it's possible to check if some language preference is saved
            val language = SettingsStorageImpl(newBase).getLanguage()
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

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navController.back()
            }
        })

        if (supportFragmentManager.fragments.isEmpty()) {
            openFirstFragment()
        }
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
        compositeDisposable.add(
            repository.userCredentialsCache.getUserCredentials()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        compositeDisposable.add(
                            mainAPI.loginUser(
                                UserCredentialsDto(
                                    it.login,
                                    it.password
                                )
                            ).subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(
                                    {
                                        if (LocationUtils.hasLocationPermission(this)) {
                                            navController.toMain()
                                        } else {
                                            navController.toLocationDialog()
                                        }
                                    },
                                    { throwable ->
                                        if (
                                            throwable is UnknownHostException
                                            || throwable is ConnectException
                                            || throwable is SocketTimeoutException
                                        ) {
                                            Log.i(
                                                TAG,
                                                "openFirstFragment: no connection, but user authorized previously",
                                                throwable
                                            )
                                            navController.toMain()
                                        } else {
                                            Log.e(
                                                TAG,
                                                "openFirstFragment: not authorized!",
                                                throwable
                                            )
                                            navController.toAuth()
                                        }
                                    }
                                )
                        )
                    },
                    {
                        navController.toAuth()
                        Log.e(TAG, "openFirstFragment: error while fetching cached user id!", it)
                    }
                )
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

    companion object {
        private const val TAG = "Main Activity Class"
    }
}