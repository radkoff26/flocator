package ru.flocator.core.connection

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import ru.flocator.core.connection.internal.CompletableConnectionWrapper
import ru.flocator.core.connection.internal.ObservableConnectionWrapper
import ru.flocator.core.connection.internal.SingleConnectionWrapper
import ru.flocator.core.connection.live_data.ConnectionLiveData

interface ConnectionWrapper<T> {
    companion object {
        fun <T : Any> of(
            single: Single<T>,
            connectionLiveData: ConnectionLiveData
        ): ConnectionWrapper<Single<T>> =
            SingleConnectionWrapper(
                single,
                connectionLiveData
            )

        fun <T : Any> of(
            observable: Observable<T>,
            connectionLiveData: ConnectionLiveData
        ): ConnectionWrapper<Observable<T>> =
            ObservableConnectionWrapper(
                observable,
                connectionLiveData
            )

        fun of(
            completable: Completable,
            connectionLiveData: ConnectionLiveData
        ): ConnectionWrapper<Completable> =
            CompletableConnectionWrapper(
                completable,
                connectionLiveData
            )
    }

    fun connect(): T
}