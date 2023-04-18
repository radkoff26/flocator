package com.example.flocator

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.flocator.authentication.authorization.AuthFragment
import com.example.flocator.common.config.SharedPreferencesContraction
import com.example.flocator.main.ui.main.MainFragment
import com.example.flocator.common.utils.FragmentNavigationUtils
import com.example.flocator.main.config.BundleArgumentsContraction
import com.example.flocator.main.ui.photo.PhotoPagerFragment
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

        PhotoPagerFragment().apply {
            arguments = Bundle().apply {
                putInt(BundleArgumentsContraction.PhotoPagerFragment.POSITION, 0)
                putStringArrayList(
                    BundleArgumentsContraction.PhotoPagerFragment.URI_LIST,
                    arrayListOf("https://miro.medium.com/v2/resize:fit:192/format:webp/1*i2i5fEF0iRU6B8QfLMw4IQ.png")
                )
            }
            show(supportFragmentManager, "TAG")
        }
    }
}