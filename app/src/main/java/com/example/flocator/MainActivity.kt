package com.example.flocator

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.MutableLiveData
import com.example.flocator.main.fragments.MainFragment
import com.example.flocator.main.fragments.State
import com.yandex.mapkit.MapKitFactory

class MainActivity : AppCompatActivity() {
    lateinit var launcher: ActivityResultLauncher<String>
    val stateLiveData = MutableLiveData(State(emptyList()))

    companion object {
        const val API_KEY = "fd3ecdab-a39c-4b8b-a215-44a8458a84bf"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapKitFactory.setApiKey(API_KEY)
        MapKitFactory.initialize(this) // TODO: remove from onCreate
        setContentView(R.layout.activity_main)
        supportActionBar!!.hide()
        launcher = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { result ->
            stateLiveData.value = State(result)
        }
        supportFragmentManager.beginTransaction()
            .add(R.id.fragment_container, MainFragment())
            .commit()
    }
}