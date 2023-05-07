package com.example.flocator.common.connection.watcher

import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.net.InetSocketAddress
import java.net.Socket
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConnectionLiveData @Inject constructor(
    @ApplicationContext context: Context
) : LiveData<Boolean>() {
    private val connectivityManager =
        context.getSystemService(ConnectivityManager::class.java)
    private val compositeDisposable = CompositeDisposable()

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            Log.d(TAG, "networkCallback: CONNECTED!")
            checkConnection()
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            Log.d(TAG, "networkCallback: LOST CONNECTION!")
            checkConnection()
        }

        override fun onUnavailable() {
            super.onUnavailable()
            Log.d(TAG, "networkCallback: UNAVAILABLE!")
            checkConnection()
        }

        override fun onLosing(network: Network, maxMsToLive: Int) {
            super.onLosing(network, maxMsToLive)
            Log.d(TAG, "networkCallback: LOSING CONNECTION!")
            checkConnection()
        }

        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            super.onCapabilitiesChanged(network, networkCapabilities)
            Log.d(TAG, "networkCallback: CAPABILITIES CHANGED!")
        }

        override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
            super.onLinkPropertiesChanged(network, linkProperties)
            Log.d(TAG, "networkCallback: LINK PROPS CHANGED!")
        }

        override fun onBlockedStatusChanged(network: Network, blocked: Boolean) {
            super.onBlockedStatusChanged(network, blocked)
            Log.d(TAG, "networkCallback: BLOCKED STATUS CHANGED! $blocked")
        }
    }

    init {
        startListening()
    }

    private fun startListening() {
        connectivityManager.registerDefaultNetworkCallback(networkCallback)
        checkConnection()
    }

    private fun stopListening() {
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    private fun checkConnection() {
        compositeDisposable.add(
            isInternetConnected()
                .subscribe(
                    { isConnected ->
                        if (value != isConnected) {
                            postValue(isConnected)
                        }
                        if (isConnected) {
                            Log.d(TAG, "CONNECTED!")
                        } else {
                            Log.d(TAG, "LOST CONNECTION!")
                        }
                    },
                    {
                        if (value!!) {
                            postValue(false)
                        }
                        Log.e(TAG, "error while checking connection!", it)
                    }
                )
        )
    }

    private fun isInternetConnected(): Single<Boolean> {
        return Single.create {
            try {
                Socket().use { socket ->
                    socket.connect(InetSocketAddress("8.8.8.8", 53), ADMITTED_WAIT_TIMEOUT)
                    Log.d(TAG, "isInternetConnected: CONNECTED TO SOCKET!")
                    it.onSuccess(true)
                }
            } catch (e: Throwable) {
                val networkCapabilities =
                    connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
                Log.d(TAG, "isInternetConnected: CANNOT CONNECT TO SOCKET!")
                it.onSuccess(
                    networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                        ?: false
                )
            }
        }.subscribeOn(Schedulers.io())
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
        stopListening()
        compositeDisposable.dispose()
    }

    companion object {
        const val TAG = "Connection Live Data"
        const val ADMITTED_WAIT_TIMEOUT = 3000
    }
}