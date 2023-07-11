package ru.flocator.core_location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng

class LocationLiveData(private val context: Context) : LiveData<LatLng?>(), DefaultLifecycleObserver {
    private val fusedLocationProviderClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    private val locationRequest by lazy {
        LocationRequest.Builder(TIME_INTERVAL)
            .setWaitForAccurateLocation(true)
            .setMinUpdateDistanceMeters(METERS_INTERVAL)
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setGranularity(Granularity.GRANULARITY_FINE)
            .build()
    }
    private val locationListener = LocationListener { p0 ->
        value = LatLng(
            p0.latitude,
            p0.longitude
        )
    }

    override fun observe(owner: LifecycleOwner, observer: Observer<in LatLng?>) {
        super.observe(owner, observer)
        owner.lifecycle.addObserver(this)
    }

    override fun onResume(owner: LifecycleOwner) {
        startListening()
    }

    override fun onPause(owner: LifecycleOwner) {
        stopListening()
    }

    private fun startListening() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                locationListener,
                Looper.getMainLooper()
            )
        }
    }

    private fun stopListening() {
        fusedLocationProviderClient.removeLocationUpdates(locationListener)
    }

    companion object {
        private const val TIME_INTERVAL = 2000L
        private const val METERS_INTERVAL = 5f
    }
}