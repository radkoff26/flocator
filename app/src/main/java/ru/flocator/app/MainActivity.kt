package ru.flocator.app

import android.os.Bundle
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import ru.flocator.app.controller.NavControllerImpl
import ru.flocator.app.data_source.MainAPI
import ru.flocator.core_api.api.AppRepository
import ru.flocator.core_controller.NavController
import ru.flocator.core_controller.NavigationRoot
import ru.flocator.core_dto.auth.UserCredentialsDto
import ru.flocator.core_utils.LocationUtils
import java.net.ConnectException
import java.net.UnknownHostException
import javax.inject.Inject

class MainActivity : AppCompatActivity(), NavigationRoot {

    private val compositeDisposable = CompositeDisposable()

    @Inject
    lateinit var repository: AppRepository

    @Inject
    lateinit var mainAPI: MainAPI

    override lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        DaggerMainActivityComponent.builder()
            .context(applicationContext)
            .build()
            .inject(this)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
        navController = NavControllerImpl(this)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navController.back()
            }
        })

        if (supportFragmentManager.fragments.isEmpty()) {
            openFirstFragment()
        }
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
                            )
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(
                                    {
                                        if (LocationUtils.hasLocationPermission(this)) {
                                            navController.toMain()
                                                .clearAll()
                                                .commit()
                                        } else {
                                            navController.toLocationDialog()
                                                .clearAll()
                                                .commit()
                                        }
                                    },
                                    { throwable ->
                                        if (throwable is UnknownHostException || throwable is ConnectException) {
                                            Log.i(
                                                TAG,
                                                "openFirstFragment: no connection, but user authorized previously",
                                                throwable
                                            )
                                            navController.toMain()
                                                .clearAll()
                                                .commit()
                                        } else {
                                            Log.e(
                                                TAG,
                                                "openFirstFragment: not authorized!",
                                                throwable
                                            )
                                            navController.toAuth()
                                                .clearAll()
                                                .commit()
                                        }
                                    }
                                )
                        )
                    },
                    {
                        navController.toAuth()
                            .clearAll()
                            .commit()
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