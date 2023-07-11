package ru.flocator.core_connection.internal

import androidx.lifecycle.Observer
import ru.flocator.core_connection.live_data.ConnectionLiveData
import ru.flocator.core_connection.ConnectionWrapper
import ru.flocator.core_exceptions.LostConnectionException
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable

internal class CompletableConnectionWrapper(
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
            connectionLiveData.postObserveForever(observer!!)
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
                connectionLiveData.postRemoveObserver(observer!!)
            }
    }
}