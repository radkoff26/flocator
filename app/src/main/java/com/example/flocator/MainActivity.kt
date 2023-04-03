package com.example.flocator

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import com.example.flocator.authreg.fragments.AuthFragment
import com.example.flocator.authreg.fragments.LocationRequestFragment
import com.yandex.mapkit.MapKitFactory


class MainActivity : AppCompatActivity() {
    companion object {
        const val COARSE_REQUEST_CODE = 100
        const val FINE_REQUEST_CODE = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState)
        MapKitFactory.initialize(this)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
//        if (ActivityCompat.checkSelfPermission(
//                this, Manifest.permission.ACCESS_FINE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
//                this, Manifest.permission.ACCESS_COARSE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            requestPermissions(
//                arrayOf(
//                    Manifest.permission.ACCESS_FINE_LOCATION
//                ), FINE_REQUEST_CODE
//            )
//            requestPermissions(
//                arrayOf(
//                    Manifest.permission.ACCESS_COARSE_LOCATION
//                ), COARSE_REQUEST_CODE
//            )
//            return
//        }
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