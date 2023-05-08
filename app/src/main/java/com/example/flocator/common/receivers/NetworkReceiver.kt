package com.example.flocator.common.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.flocator.common.config.Actions
import com.example.flocator.common.connection.watcher.ConnectionLiveData
import com.example.flocator.common.connection.watcher.MutableConnectionLiveData
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.net.InetSocketAddress
import java.net.Socket

class NetworkReceiver: BroadcastReceiver() {
    private val compositeDisposable = CompositeDisposable()

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

    fun stop() {
        compositeDisposable.dispose()
    }

    private fun checkConnection() {
        compositeDisposable.add(
            isInternetConnected()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { isConnected ->
                    _networkState.setValue(isConnected)
                }
        )
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