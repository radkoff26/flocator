package ru.flocator.feature_main.internal.data.repository

import android.util.Log
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import ru.flocator.data.data_store.point.UserLocationDataStoreManager
import ru.flocator.data.data_store.point.UserLocationPoint
import ru.flocator.data.database.dao.UserDao
import ru.flocator.data.database.entities.User
import ru.flocator.data.models.location.Coordinates
import ru.flocator.feature_main.internal.data.data_source.MainDataSource
import ru.flocator.feature_main.internal.data.model.user_name.UsernameDto
import javax.inject.Inject

internal class UserRepository @Inject constructor(
    private val mainDataSource: MainDataSource,
    private val userDao: UserDao,
    private val locationDataStoreManager: UserLocationDataStoreManager
) {

    fun getAllFriendsFromCache(): Single<List<User>> =
        userDao.getAllFriends().subscribeOn(Schedulers.io())

    fun getAllFriendsOfUser(): Single<List<User>> =
        mainDataSource.getUserFriendsLocated()
            .subscribeOn(Schedulers.io())
            .doAfterSuccess {
                saveNewFriendsToCache(it)
            }

    private fun saveNewFriendsToCache(newFriends: List<User>) {
        val compositeDisposable = CompositeDisposable()
        compositeDisposable.add(
            userDao.updateTable(newFriends)
                .subscribeOn(Schedulers.io())
                .doOnError { throwable ->
                    Log.e(
                        TAG,
                        "getAllFriendsOfUser: error while saving friends to cache!",
                        throwable
                    )
                }
                .doFinally {
                    compositeDisposable.dispose()
                }
                .subscribe()
        )
    }

    fun getUsername(userId: Long): Single<UsernameDto> =
        mainDataSource.getUsername(userId).subscribeOn(Schedulers.io())

    fun postUserLocation(location: Coordinates): Single<Boolean> =
        mainDataSource.updateLocation(location)
            .subscribeOn(Schedulers.io())
            .doOnSuccess {
                locationDataStoreManager.setUserLocation(
                    UserLocationPoint(
                        location.latitude,
                        location.longitude
                    )
                )
            }

    fun goOnline(): Completable {
        return mainDataSource.goOnline().subscribeOn(Schedulers.io())
    }

    fun goOffline(): Completable {
        return mainDataSource.goOffline().subscribeOn(Schedulers.io())
    }

    companion object {
        private const val TAG = "UserRepository_TAG"
    }
}