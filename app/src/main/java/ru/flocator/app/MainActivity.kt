package ru.flocator.app

import android.os.Bundle
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import ru.flocator.app.authentication.client.RetrofitClient
import ru.flocator.core_dto.auth.UserCredentialsDto
import ru.flocator.app.authentication.getlocation.LocationRequestFragment
import ru.flocator.core_api.api.MainRepository
import ru.flocator.app.main.ui.MainFragment
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import ru.flocator.app.authentication.authorization.AuthFragment
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
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                ru.flocator.core_utils.FragmentNavigationUtils.closeLastFragment(supportFragmentManager, this@MainActivity)
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
                                        if (ru.flocator.core_utils.LocationUtils.hasLocationPermission(this)) {
                                            ru.flocator.core_utils.FragmentNavigationUtils.openFragment(
                                                supportFragmentManager,
                                                MainFragment()
                                            )
                                        } else {
                                            ru.flocator.core_utils.FragmentNavigationUtils.openFragment(
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
                                            ru.flocator.core_utils.FragmentNavigationUtils.openFragment(
                                                supportFragmentManager,
                                                MainFragment()
                                            )
                                        } else {
                                            Log.e(
                                                TAG,
                                                "openFirstFragment: not authorized!",
                                                throwable
                                            )
                                            ru.flocator.core_utils.FragmentNavigationUtils.openFragment(
                                                supportFragmentManager,
                                                AuthFragment()
                                            )
                                        }
                                    }
                                )
                        )
                    },
                    {
                        ru.flocator.core_utils.FragmentNavigationUtils.openFragment(
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