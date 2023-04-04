package com.example.flocator

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import com.example.flocator.authreg.fragments.AuthFragment
import com.example.flocator.authreg.fragments.LocationRequestFragment
import com.example.flocator.utils.FragmentNavigationUtils
import com.yandex.mapkit.MapKitFactory


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
        val sharedPreferences = getSharedPreferences("USER", MODE_PRIVATE)

        // TODO: STUB!
        if (!sharedPreferences.contains("USER_ID")) {
            val editor = sharedPreferences.edit()
            editor.putLong("USER_ID", 1)
            editor.apply()
        }
        if (!sharedPreferences.contains("USER_AVATAR_URL")) {
            val editor = sharedPreferences.edit()
            editor.putString("USER_AVATAR_URL", "https://sun9-55.userapi.com/impg/2NrJDQ-paBNyKNiDFFU0ItHSxe4PmpWR-V16fA/9ZkY5ZR55gc.jpg?size=720x1280&quality=95&sign=e2343d8bb5f0039a054c4cb063486f26&type=album")
            editor.apply()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                FragmentNavigationUtils.closeLastFragment(supportFragmentManager, this@MainActivity)
            }
        })

        supportFragmentManager
            .beginTransaction()
            .add(R.id.fragment_container, AuthFragment())
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