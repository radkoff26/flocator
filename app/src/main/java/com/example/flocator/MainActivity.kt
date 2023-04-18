package com.example.flocator

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.flocator.main.ui.main.MainFragment
import com.example.flocator.common.utils.FragmentNavigationUtils
import com.yandex.mapkit.MapKitFactory
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

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

        supportFragmentManager
            .beginTransaction()
            .add(R.id.fragment_container, MainFragment())
            .commit()
    }
}