package com.example.flocator.common.receivers

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.flocator.common.config.Actions
import com.example.flocator.common.connection.live_data.ConnectionLiveData
import com.example.flocator.common.connection.live_data.MutableConnectionLiveData
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.net.InetSocketAddress
import java.net.Socket
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkReceiver @Inject constructor(): BroadcastReceiver() {
    private val _networkState = MutableConnectionLiveData()
    val networkState: ConnectionLiveData = _networkState

    init {
        checkConnection()
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent != null && intent.action == Actions.CONNECTIVITY_CHANGE) {
            checkConnection()
        }
    }

    @SuppressLint("CheckResult")
    private fun checkConnection() {
        isInternetConnected()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { isConnected ->
                _networkState.setValue(isConnected)
            }
    }

    private fun isInternetConnected(): Single<Boolean> {
        return Single.create {
            try {
                Socket().use { socket ->
                    socket.connect(
                        InetSocketAddress("8.8.8.8", 53),
                        ADMITTED_WAIT_TIMEOUT
                    )
                    it.onSuccess(true)
                }
            } catch (e: Throwable) {
                it.onSuccess(false)
            }
        }.subscribeOn(Schedulers.io())
    }

    companion object {
        const val ADMITTED_WAIT_TIMEOUT = 3000
    }
}