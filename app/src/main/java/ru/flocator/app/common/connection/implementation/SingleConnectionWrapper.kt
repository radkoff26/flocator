package ru.flocator.app.common.connection.implementation

import androidx.lifecycle.Observer
import ru.flocator.app.common.connection.live_data.ConnectionLiveData
import ru.flocator.app.common.connection.ConnectionWrapper
import ru.flocator.app.common.exceptions.LostConnectionException
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable

class SingleConnectionWrapper<T : Any>(
    private val single: Single<T>,
    private val connectionLiveData: ConnectionLiveData
) : ConnectionWrapper<Single<T>> {
    override fun connect(): Single<T> {
        val compositeDisposable = CompositeDisposable()
        var observer: Observer<Boolean>? = null
        if (!connectionLiveData.value!!) {
            return Single.error(LostConnectionException("Connection is lost!"))
        }
        return Single.create { emitter ->
            observer = Observer {
                if (!it) {
                    emitter.onError(LostConnectionException("Connection is lost!"))
                }
            }
            connectionLiveData.postObserveForever(observer!!)
            compositeDisposable.add(
                single
                    .doOnSuccess { emitter.onSuccess(it) }
                    .subscribe()
            )
        }
            .doOnDispose {
                compositeDisposable.dispose()
            }
            .doOnSuccess {
                connectionLiveData.postRemoveObserver(observer!!)
            }
    }
}