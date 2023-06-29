package ru.flocator.core_api.api

import android.graphics.Bitmap
import android.util.Log
import androidx.datastore.core.DataStore
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.flocator.cache.global.PhotoCacheManager
import ru.flocator.core_data_store.point.UserLocationPoint
import ru.flocator.core_data_store.user.data.UserCredentials
import ru.flocator.core_data_store.user.info.UserInfo
import ru.flocator.core_database.ApplicationDatabase
import ru.flocator.core_database.entities.Mark
import ru.flocator.core_database.entities.MarkPhoto
import ru.flocator.core_database.entities.MarkWithPhotos
import ru.flocator.core_database.entities.User
import ru.flocator.core_utils.LoadUtils

private typealias LoadingImage = Pair<String, Int>

class AppRepository constructor(
    private val applicationDatabase: ApplicationDatabase,
    private val userLocationDataStore: DataStore<UserLocationPoint>,
    private val userCredentialsStore: DataStore<UserCredentials>,
    private val userInfoStore: DataStore<UserInfo>,
    private val photoCacheManager: PhotoCacheManager
) {
    val cacheDatabase = CacheDatabase()
    val userCredentialsCache = UserCredentialsCache()
    val userInfoCache = UserInfoCache()
    val locationCache = LocationCache()
    val photoLoader = PhotoLoader()

    inner class CacheDatabase {
        fun updateMarks(marks: List<Mark>): Completable {
            return applicationDatabase.markDao().updateTable(marks).subscribeOn(Schedulers.io())
        }

        fun retrieveMarksFromCache(): Single<List<MarkWithPhotos>> {
            return applicationDatabase.markDao().getAllMarks().subscribeOn(Schedulers.io())
        }

        fun updateMarkPhotos(markPhotos: List<MarkPhoto>): Completable {
            return applicationDatabase.markPhotoDao().updateTable(markPhotos)
                .subscribeOn(Schedulers.io())
        }

        fun updateFriends(friends: List<User>): Completable {
            return applicationDatabase.userDao().updateTable(friends).subscribeOn(Schedulers.io())
        }

        fun retrieveFriendsFromCache(): Single<List<User>> {
            return applicationDatabase.userDao().getAllFriends().subscribeOn(Schedulers.io())
        }

        fun clearDatabase(): Completable {
            return Completable.concatArray(
                applicationDatabase.markPhotoDao().clearAll(),
                applicationDatabase.markDao().clearAll(),
                applicationDatabase.userDao().clearAll()
            )
        }
    }

    inner class UserCredentialsCache {
        fun getUserCredentials(): Single<UserCredentials> {
            return Single.create { emitter ->
                CoroutineScope(Dispatchers.IO).launch {
                    userCredentialsStore.data.collect {
                        if (it == UserCredentials.DEFAULT) {
                            emitter.onError(NoSuchElementException("Data is not yet assigned!"))
                        } else {
                            emitter.onSuccess(it)
                        }
                    }
                }
            }.subscribeOn(Schedulers.io())
        }

        fun updateUserCredentials(userCredentials: UserCredentials) {
            CoroutineScope(Dispatchers.IO).launch {
                userCredentialsStore.updateData { userCredentials }
            }
        }

        fun clearUserCredentials() {
            updateUserCredentials(UserCredentials.DEFAULT)
        }
    }

    inner class UserInfoCache {
        fun getUserInfo(): Single<UserInfo> {
            return Single.create { emitter ->
                CoroutineScope(Dispatchers.IO).launch {
                    userInfoStore.data.collect {
                        if (it == UserInfo.DEFAULT) {
                            emitter.onError(NoSuchElementException("Data is not yet assigned!"))
                        } else {
                            emitter.onSuccess(it)
                        }
                    }
                }
            }.subscribeOn(Schedulers.io())
        }

        fun updateUserInfo(userInfo: UserInfo) {
            CoroutineScope(Dispatchers.IO).launch {
                userInfoStore.updateData { userInfo }
            }
        }

        fun clearUserInfo() {
            updateUserInfo(UserInfo.DEFAULT)
        }
    }

    inner class LocationCache {
        fun getUserLocationData(): Single<UserLocationPoint> {
            return Single.create { emitter ->
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

        fun updateUserLocationData(userLocationPoint: UserLocationPoint) {
            CoroutineScope(Dispatchers.IO).launch {
                userLocationDataStore.updateData { userLocationPoint }
            }
        }

        fun clearLocation() {
            updateUserLocationData(UserLocationPoint.DEFAULT)
        }
    }

    inner class PhotoLoader {
        private val state: MutableSet<LoadingImage> = HashSet()

        fun getPhoto(uri: String, qualityFactor: Int = 100): Single<Bitmap> {
            if (photoCacheManager.isPhotoCached(uri, qualityFactor)) {
                return Single.just(photoCacheManager.getPhotoFromCache(uri, qualityFactor)!!)
                    .subscribeOn(Schedulers.io())
            }
            val processState = LoadingImage(uri, qualityFactor)
            state.add(processState)
            return LoadUtils.loadPictureFromUrl(uri, qualityFactor)
                .doOnSuccess {
                    try {
                        photoCacheManager.savePhotoToCache(uri, it, qualityFactor)
                    } catch (e: Exception) {
                        // Ignore any exceptions to prevent from onError callback
                        Log.e(TAG, "getPhoto: error while saving to cache!", e)
                    } finally {
                        state.remove(processState)
                    }
                }
                .doOnError {
                    state.remove(processState)
                }
                .subscribeOn(Schedulers.io())
        }
    }

    fun clearAllCache(): Completable {
        userInfoCache.clearUserInfo()
        userCredentialsCache.clearUserCredentials()
        locationCache.clearLocation()
        return cacheDatabase.clearDatabase().subscribeOn(Schedulers.io())
    }

    companion object {
        private const val TAG = "Main Repository"
    }
}