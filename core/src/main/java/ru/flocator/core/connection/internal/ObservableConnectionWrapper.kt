package ru.flocator.core.connection.internal

import androidx.lifecycle.Observer
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import ru.flocator.core.connection.ConnectionWrapper
import ru.flocator.core.connection.live_data.ConnectionLiveData
import ru.flocator.core.exceptions.LostConnectionException

internal class ObservableConnectionWrapper<T : Any>(
    private val observable: Observable<T>,
    private val connectionLiveData: ConnectionLiveData
) : ConnectionWrapper<Observable<T>> {
    override fun connect(): Observable<T> {
        val compositeDisposable = CompositeDisposable()
        var observer: Observer<Boolean>? = null
        if (!connectionLiveData.value!!) {
            return Observable.error(LostConnectionException("Connection is lost!"))
        }
        return Observable.create { emitter ->
            observer = Observer {
                if (!it) {
                    emitter.onError(LostConnectionException("Connection is lost!"))
                }
            }
            connectionLiveData.postObserveForever(observer!!)
            compositeDisposable.add(
                observable
                    .doOnNext { emitter.onNext(it) }
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