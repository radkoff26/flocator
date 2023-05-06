package com.example.flocator.main.handlers

import android.content.Context
import android.location.Location
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.flocator.common.utils.LocationUtils
import com.google.android.gms.location.LocationServices
import java.util.function.Consumer

class UserLocationHandler(
    private val context: Context,
    lifecycleOwner: LifecycleOwner,
    private var userLocationListener: Consumer<Location>,
) : DefaultLifecycleObserver {
    private val fusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    private val handler = Handler(Looper.getMainLooper())
    private var isRunning = false

    init {
        lifecycleOwner.lifecycle.addObserver(this)
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        isRunning = true
        handler.post(this::getCurrentLocation)
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        isRunning = false
    }

    private fun getCurrentLocation() {
        if (!isRunning) {
            return
        }
        LocationUtils.getCurrentLocation(context, fusedLocationProviderClient) {
            if (it != null) {
                userLocationListener.accept(it)
            }
            handler.postDelayed(this::getCurrentLocation, FREQUENCY)
        }
    }

    companion object {
        const val FREQUENCY = 5000L
    }
}