package com.example.flocator

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.flocator.logreg.fragments.AuthFragment
import com.example.flocator.main.ui.fragments.MainFragment
import com.yandex.mapkit.MapKitFactory


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState)
        MapKitFactory.initialize(this)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
        supportFragmentManager
            .beginTransaction()
            .add(R.id.fragment_container, AuthFragment())
            .commit()
    }
}