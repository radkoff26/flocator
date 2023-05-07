package com.example.flocator

import android.os.Bundle
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.flocator.authentication.authorization.AuthFragment
import com.example.flocator.authentication.client.RetrofitClient
import com.example.flocator.authentication.client.dto.UserCredentialsDto
import com.example.flocator.authentication.getlocation.LocationRequestFragment
import com.example.flocator.common.repository.MainRepository
import com.example.flocator.common.utils.FragmentNavigationUtils
import com.example.flocator.common.utils.LocationUtils
import com.example.flocator.main.ui.main.MainFragment
import com.yandex.mapkit.MapKitFactory
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.net.ConnectException
import java.net.UnknownHostException
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val compositeDisposable = CompositeDisposable()

    @Inject
    lateinit var repository: MainRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        MapKitFactory.initialize(this)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                FragmentNavigationUtils.closeLastFragment(supportFragmentManager, this@MainActivity)
            }
        })

        if (supportFragmentManager.fragments.isEmpty()) {
            openFirstFragment()
        }
    }

    private fun openFirstFragment() {
        compositeDisposable.add(
            repository.userDataCache.getUserData()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        compositeDisposable.add(
                            RetrofitClient.authenticationApi.loginUser(
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
                                            FragmentNavigationUtils.openFragment(
                                                supportFragmentManager,
                                                MainFragment()
                                            )
                                        } else {
                                            FragmentNavigationUtils.openFragment(
                                                supportFragmentManager,
                                                LocationRequestFragment()
                                            )
                                        }
                                    },
                                    { throwable ->
                                        if (throwable is UnknownHostException || throwable is ConnectException) {
                                            Log.i(
                                                TAG,
                                                "openFirstFragment: no connection, but user authorized previously",
                                                throwable
                                            )
                                            FragmentNavigationUtils.openFragment(
                                                supportFragmentManager,
                                                MainFragment()
                                            )
                                        } else {
                                            Log.e(
                                                TAG,
                                                "openFirstFragment: not authorized!",
                                                throwable
                                            )
                                            FragmentNavigationUtils.openFragment(
                                                supportFragmentManager,
                                                AuthFragment()
                                            )
                                        }
                                    }
                                )
                        )
                    },
                    {
                        FragmentNavigationUtils.openFragment(
                            supportFragmentManager,
                            AuthFragment()
                        )
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