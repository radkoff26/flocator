package com.example.flocator

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.flocator.common.config.SharedPreferencesContraction
import com.example.flocator.main.ui.fragments.MainFragment
import com.example.flocator.common.utils.FragmentNavigationUtils
import com.yandex.mapkit.MapKitFactory
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    companion object {
        const val COARSE_REQUEST_CODE = 100
        const val FINE_REQUEST_CODE = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        MapKitFactory.initialize(this)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
        val sharedPreferences = getSharedPreferences(SharedPreferencesContraction.User.prefs_name, MODE_PRIVATE)

        // TODO: STUB!
        if (!sharedPreferences.contains(SharedPreferencesContraction.User.USER_ID)) {
            val editor = sharedPreferences.edit()
            editor.putLong(SharedPreferencesContraction.User.USER_ID, 1)
            editor.apply()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                FragmentNavigationUtils.closeLastFragment(supportFragmentManager, this@MainActivity)
            }
        })

        supportFragmentManager
            .beginTransaction()
            .add(R.id.fragment_container, MainFragment())
            .commit()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            COARSE_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    finish()
                }
            }
            FINE_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    finish()
                }
            }
        }
    }
}