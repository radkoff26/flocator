package ru.flocator.core_connection.live_data

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import ru.flocator.core_dependency.Dependency

open class ConnectionLiveData : LiveData<Boolean>(), Dependency {
    init {
        value = false
    }

    fun postObserveForever(observer: Observer<Boolean>) {
        Completable.create {
            observeForever(observer)
            it.onComplete()
        }.subscribeOn(AndroidSchedulers.mainThread()).subscribe()
    }

    fun postRemoveObserver(observer: Observer<Boolean>) {
        Completable.create {
            removeObserver(observer)
            it.onComplete()
        }.subscribeOn(AndroidSchedulers.mainThread()).subscribe()
    }
}