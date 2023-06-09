package ru.flocator.app.common.connection

import ru.flocator.app.common.connection.implementation.CompletableConnectionWrapper
import ru.flocator.app.common.connection.implementation.ObservableConnectionWrapper
import ru.flocator.app.common.connection.implementation.SingleConnectionWrapper
import ru.flocator.app.common.connection.live_data.ConnectionLiveData
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface ConnectionWrapper<T> {
    companion object {
        fun <T : Any> of(single: Single<T>, connectionLiveData: ConnectionLiveData): ConnectionWrapper<Single<T>> =
            SingleConnectionWrapper(
                single,
                connectionLiveData
            )
        fun <T: Any> of(observable: Observable<T>, connectionLiveData: ConnectionLiveData): ConnectionWrapper<Observable<T>> =
            ObservableConnectionWrapper(
                observable,
                connectionLiveData
            )
        fun of(completable: Completable, connectionLiveData: ConnectionLiveData): ConnectionWrapper<Completable> =
            CompletableConnectionWrapper(
                completable,
                connectionLiveData
            )
    }

    fun connect(): T
}