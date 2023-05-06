package com.example.flocator.common.connection.watcher

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConnectionLiveData @Inject constructor(
    @ApplicationContext context: Context
) : LiveData<Boolean>() {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val compositeDisposable = CompositeDisposable()
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            Log.d(TAG, "onAvailable: CONNECTED!")
            postValue(true)
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            Log.d(TAG, "onAvailable: LOST CONNECTION!")
            postValue(false)
        }
    }

    init {
        value = connectivityManager.isDefaultNetworkActive
        connectivityManager.registerNetworkCallback(getDefaultRequest(), networkCallback)
    }

    fun observeForeverAsync(observer: Observer<Boolean>) {
        compositeDisposable.add(
            Completable.create {
                observeForever(observer)
            }
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe()
        )
    }

    fun removeObserverAsync(observer: Observer<Boolean>) {
        compositeDisposable.add(
            Completable.create {
                removeObserver(observer)
            }
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe()
        )
    }

    override fun onInactive() {
        super.onInactive()
        connectivityManager.unregisterNetworkCallback(networkCallback)
        compositeDisposable.dispose()
    }

    private fun getDefaultRequest() = NetworkRequest.Builder().build()

    companion object {
        const val TAG = "Connection Live Data"
    }
}