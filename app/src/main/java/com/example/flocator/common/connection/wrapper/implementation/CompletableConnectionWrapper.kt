package com.example.flocator.common.connection.wrapper.implementation

import androidx.lifecycle.Observer
import com.example.flocator.common.connection.watcher.ConnectionLiveData
import com.example.flocator.common.connection.wrapper.ConnectionWrapper
import com.example.flocator.common.exceptions.LostConnectionException
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable

class CompletableConnectionWrapper(
    private val observable: Completable,
    private val connectionLiveData: ConnectionLiveData
) : ConnectionWrapper<Completable> {
    override fun connect(): Completable {
        val compositeDisposable = CompositeDisposable()
        var observer: Observer<Boolean>? = null
        if (!connectionLiveData.value!!) {
            return Completable.error(LostConnectionException("Connection is lost!"))
        }
        return Completable.create { emitter ->
            observer = Observer {
                if (!it) {
                    emitter.onError(LostConnectionException("Connection is lost!"))
                }
            }
            connectionLiveData.observeForeverAsync(observer!!)
            compositeDisposable.add(
                observable
                    .doOnComplete { emitter.onComplete() }
                    .subscribe()
            )
        }
            .doOnDispose {
                compositeDisposable.dispose()
            }
            .doOnComplete {
                connectionLiveData.removeObserverAsync(observer!!)
            }
    }
}