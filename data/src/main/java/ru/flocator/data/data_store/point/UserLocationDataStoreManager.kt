package ru.flocator.data.data_store.point

import androidx.datastore.core.DataStore
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect

class UserLocationDataStoreManager(
    private val userLocationDataStore: DataStore<UserLocationPoint>
) {

    fun getUserLocation(): Single<UserLocationPoint> {
        return Single.create<UserLocationPoint> { emitter ->
            CoroutineScope(Dispatchers.IO).launch {
                userLocationDataStore.data.collect {
                    if (it == UserLocationPoint.DEFAULT) {
                        emitter.onError(NoSuchElementException("Data is not yet assigned!"))
                    } else {
                        emitter.onSuccess(it)
                    }
                }
            }
        }.subscribeOn(Schedulers.io())
    }

    fun setUserLocation(userLocationPoint: UserLocationPoint) {
        CoroutineScope(Dispatchers.IO).launch {
            userLocationDataStore.updateData { userLocationPoint }
        }
    }

    fun clearUserLocation() {
        setUserLocation(UserLocationPoint.DEFAULT)
    }
}