package ru.flocator.data.data_store.info

import androidx.datastore.core.DataStore
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.create
import retrofit2.http.GET
import ru.flocator.data.api.ApiPaths

class UserInfoMediator(dataStore: DataStore<UserInfo>, retrofit: Retrofit) {
    private val dataStoreManager = UserInfoDataStoreManager(dataStore)
    private val dataSource: UserInfoDataSource = retrofit.create()

    fun getUserInfo(): Single<UserInfo> {
        val compositeDisposable = CompositeDisposable()
        return Single.create { emitter ->
            val currentUserInfo = try {
                dataStoreManager.getUserInfo().blockingGet()
            } catch (t: Throwable) {
                null
            }
            compositeDisposable.add(
                dataSource.getCurrentUser()
                    .subscribeOn(Schedulers.io())
                    .subscribe(
                        {
                            if (it != currentUserInfo) {
                                dataStoreManager.setUserInfo(it)
                            }
                            emitter.onSuccess(it)
                            compositeDisposable.dispose()
                        },
                        {
                            if (currentUserInfo == null) {
                                emitter.onError(it)
                            } else {
                                emitter.onSuccess(currentUserInfo)
                            }
                            compositeDisposable.dispose()
                        }
                    )
            )
        }.subscribeOn(Schedulers.io())
    }

    fun clearData() {
        dataStoreManager.clearUserInfo()
    }
}

private interface UserInfoDataSource {

    @GET(ApiPaths.USER_GET_USER)
    fun getCurrentUser(): Single<UserInfo>
}