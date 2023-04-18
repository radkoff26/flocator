package com.example.flocator

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.flocator.authentication.authorization.AuthFragment
import com.example.flocator.authentication.client.RetrofitClient
import com.example.flocator.authentication.client.dto.UserCredentialsDto
import com.example.flocator.authentication.getlocation.LocationRequestFragment
import com.example.flocator.common.storage.SharedStorage
import com.example.flocator.main.ui.main.MainFragment
import com.example.flocator.common.utils.FragmentNavigationUtils
import com.example.flocator.common.utils.LocationUtils
import com.yandex.mapkit.MapKitFactory
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val compositeDisposable = CompositeDisposable()

    @Inject
    lateinit var sharedStorage: SharedStorage

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

        openFirstFragment()
    }

    private fun openFirstFragment() {
        if (!sharedStorage.hasUserData()) {
            FragmentNavigationUtils.openFragment(
                supportFragmentManager,
                AuthFragment()
            )
            return
        }
        val login = sharedStorage.getLogin()!!
        val password = sharedStorage.getPassword()!!
        compositeDisposable.add(
            RetrofitClient.authenticationApi.loginUser(UserCredentialsDto(login, password))
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
                    {
                        FragmentNavigationUtils.openFragment(
                            supportFragmentManager,
                            AuthFragment()
                        )
                    }
                )
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }
}