package ru.flocator.data.data_store.info

import androidx.datastore.core.DataStore
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect

internal class UserInfoDataStoreManager(
    private val userInfoStore: DataStore<UserInfo>
) {
    
    fun getUserInfo(): Single<UserInfo> {
        return Single.create<UserInfo> { emitter ->
            CoroutineScope(Dispatchers.IO).launch {
                userInfoStore.data.collect { value ->
                    if (value == UserInfo.DEFAULT) {
                        emitter.onError(NoSuchElementException("Data is not yet assigned!"))
                    } else {
                        emitter.onSuccess(value)
                    }
                }
            }
        }.subscribeOn(Schedulers.io())
    }

    fun setUserInfo(userInfo: UserInfo) {
        CoroutineScope(Dispatchers.IO).launch {
            userInfoStore.updateData { userInfo }
        }
    }

    fun clearUserInfo() {
        setUserInfo(UserInfo.DEFAULT)
    }
}