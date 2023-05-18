package com.example.flocator.common.repository

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.datastore.core.DataStore
import com.example.flocator.common.cache.global.PhotoCacheManager
import com.example.flocator.common.connection.ConnectionWrapper
import com.example.flocator.common.connection.live_data.ConnectionLiveData
import com.example.flocator.common.storage.db.ApplicationDatabase
import com.example.flocator.common.storage.db.entities.Mark
import com.example.flocator.common.storage.db.entities.MarkPhoto
import com.example.flocator.common.storage.db.entities.MarkWithPhotos
import com.example.flocator.common.storage.db.entities.User
import com.example.flocator.common.storage.store.user.info.UserInfo
import com.example.flocator.common.storage.store.point.UserLocationPoint
import com.example.flocator.common.storage.store.user.data.UserData
import com.example.flocator.common.utils.LoadUtils
import com.example.flocator.community.api.UserApi
import com.example.flocator.main.api.ClientAPI
import com.example.flocator.main.api.GeocoderAPI
import com.example.flocator.main.data.response.AddressResponse
import com.example.flocator.main.models.dto.MarkDto
import com.example.flocator.main.models.dto.UserLocationDto
import com.example.flocator.main.ui.add_mark.data.AddMarkDto
import com.example.flocator.settings.SettingsAPI
import com.google.gson.Gson
import com.yandex.mapkit.geometry.Point
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.sql.Timestamp
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MainRepository @Inject constructor(
    private val clientAPI: ClientAPI,
    private val geocoderAPI: GeocoderAPI,
    private val userApi: UserApi,
    private val applicationDatabase: ApplicationDatabase,
    private val userLocationDataStore: DataStore<UserLocationPoint>,
    private val userDataStore: DataStore<UserData>,
    private val userInfoStore: DataStore<UserInfo>,
    private val photoCacheManager: PhotoCacheManager,
    private val settingsAPI: SettingsAPI,
    private val connectionLiveData: ConnectionLiveData
) {
    val restApi = RestApi()
    val cacheDatabase = CacheDatabase()
    val userDataCache = UserDataCache()
    val userInfoCache = UserInfoCache()
    val locationCache = LocationCache()
    val photoLoader = PhotoLoader()

    inner class RestApi {
        fun getAllFriendsOfUser(userId: Long): Single<List<User>> {
            val compositeDisposable = CompositeDisposable()
            return ConnectionWrapper.of(
                Single.create { emitter ->
                    compositeDisposable.add(
                        ConnectionWrapper.of(
                            clientAPI.getUserFriendsLocated(userId)
                                .subscribeOn(Schedulers.io())
                                .observeOn(Schedulers.io()),
                            connectionLiveData
                        )
                            .connect()
                            .subscribe(
                                {
                                    emitter.onSuccess(it)
                                    compositeDisposable.add(
                                        cacheDatabase.updateFriends(it)
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(Schedulers.io())
                                            .doOnError { throwable ->
                                                Log.e(
                                                    TAG,
                                                    "getAllFriendsOfUser: error while saving friends to cache!",
                                                    throwable
                                                )
                                            }
                                            .subscribe()
                                    )
                                },
                                {
                                    Log.e(
                                        TAG,
                                        "getAllFriendsOfUser: error while fetching data from server!",
                                        it
                                    )
                                    emitter.onError(it)
                                }
                            )
                    )
                }.subscribeOn(Schedulers.io()),
                connectionLiveData
            ).connect().doOnDispose { compositeDisposable.dispose() }
        }

        fun getMarksForUser(userId: Long): Single<List<MarkWithPhotos>> {
            val compositeDisposable = CompositeDisposable()
            return ConnectionWrapper.of(
                Single.create<List<MarkWithPhotos>> { emitter ->
                    compositeDisposable.add(
                        ConnectionWrapper.of(
                            clientAPI.getUserAndFriendsMarks(userId)
                                .subscribeOn(Schedulers.io())
                                .observeOn(Schedulers.io()),
                            connectionLiveData
                        )
                            .connect()
                            .subscribe(
                                {
                                    val marks = it.map(MarkDto::toMarkWithPhotos)
                                    emitter.onSuccess(marks)
                                    val photos = marks.map(MarkWithPhotos::photos).flatten()
                                    Completable.concatArray(
                                        cacheDatabase.updateMarks(marks.map(MarkWithPhotos::mark)),
                                        cacheDatabase.updateMarkPhotos(photos)
                                    )
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(Schedulers.io())
                                        .doOnError { throwable ->
                                            Log.e(
                                                TAG,
                                                "getAllFriendsOfUser: error while saving marks to cache!",
                                                throwable
                                            )
                                        }
                                        .subscribe()
                                },
                                {
                                    emitter.onError(it)
                                    Log.e(
                                        TAG,
                                        "getMarksForUser: error while fetching marks from server!",
                                        it
                                    )
                                }
                            ),
                    )
                },
                connectionLiveData
            ).connect().doOnDispose { compositeDisposable.dispose() }
        }

        fun postMark(markDto: AddMarkDto, photos: Set<Map.Entry<Uri, ByteArray>>): Completable {
            val addMarkDtoToPost: RequestBody = RequestBody.create(
                MediaType.parse("application/json"),
                Gson().toJson(markDto)
            )
            val photosToPost: List<MultipartBody.Part> = photos.map {
                val requestBody = RequestBody.create(MediaType.parse("image/*"), it.value)
                MultipartBody.Part.createFormData("photos", it.key.toString(), requestBody)
            }
            return clientAPI.postMark(addMarkDtoToPost, photosToPost).subscribeOn(Schedulers.io())
        }

        fun getMark(markId: Long, userId: Long): Single<MarkDto> {
            return clientAPI.getMark(markId, userId).subscribeOn(Schedulers.io())
        }

        fun getUser(userId: Long): Single<UserInfo> {
            return clientAPI.getUser(userId).subscribeOn(Schedulers.io())
        }

        fun postUserLocation(userId: Long, location: Point): Completable {
            return ConnectionWrapper.of(
                clientAPI.updateLocation(
                    UserLocationDto(
                        userId,
                        location
                    )
                )
                    .subscribeOn(Schedulers.io())
                    .doOnComplete {
                        locationCache.updateUserLocationData(
                            UserLocationPoint(
                                location.latitude,
                                location.longitude
                            )
                        )
                    },
                connectionLiveData
            ).connect()
        }

        fun likeMark(markId: Long, userId: Long): Completable {
            return clientAPI.likeMark(markId, userId).subscribeOn(Schedulers.io())
        }

        fun unlikeMark(markId: Long, userId: Long): Completable {
            return clientAPI.unlikeMark(markId, userId).subscribeOn(Schedulers.io())
        }

        fun getAddress(point: Point): Single<String> {
            return geocoderAPI.getAddress("${point.latitude}, ${point.longitude}")
                .map(AddressResponse::address)
                .subscribeOn(Schedulers.io())
        }

        fun getCurrentUserInfo(): Single<UserInfo> {
            return userDataCache.getUserData()
                .flatMap {
                    getUser(it.userId)
                        .subscribeOn(Schedulers.io())
                }
                .subscribeOn(Schedulers.io())
        }

        fun addFriendByLogin(userId: Long, login: String): Completable {
            return userApi.addNewFriendByLogin(userId, login).subscribeOn(Schedulers.io())
        }

        fun rejectNewFriend(userId: Long, friendId: Long): Completable{
            return userApi.rejectNewFriend(userId, friendId).subscribeOn(Schedulers.io())
        }

        fun acceptNewFriend(userId: Long, friendId: Long): Completable{
            return userApi.acceptNewFriend(userId, friendId).subscribeOn(Schedulers.io())
        }

        fun changeCurrentUserAva(ava: MultipartBody.Part): Single<Boolean> {
            return userDataCache.getUserData().flatMap {
                settingsAPI.changeAvatar(
                    it.userId,
                    ava
                )
                    .subscribeOn(Schedulers.io())
            }
                .subscribeOn(Schedulers.io())
        }

        fun changeCurrentUserBirthdate(date: Timestamp): Single<Boolean> {
            return userDataCache.getUserData().flatMap {
                settingsAPI.setBirthDate(
                    it.userId,
                    date
                )
                    .subscribeOn(Schedulers.io())
            }
                .subscribeOn(Schedulers.io())
        }

        fun changeCurrentUserName(firstName: String, lastName: String): Single<Boolean> {
            return userDataCache.getUserData().flatMap {
                settingsAPI.changeName(
                    it.userId,
                    firstName,
                    lastName
                )
                    .observeOn(Schedulers.io())
            }
                .observeOn(Schedulers.io())
        }

        fun changeCurrentUserPass(prevPass: String, pass: String): Single<Boolean> {
            return userDataCache.getUserData().flatMap {
                settingsAPI.changePassword(
                    it.userId,
                    prevPass,
                    pass
                )
                    .observeOn(Schedulers.io())
            }
                .observeOn(Schedulers.io())
        }
    }

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
    }

    inner class UserDataCache {
        fun getUserData(): Single<UserData> {
            return Single.create { emitter ->
                CoroutineScope(Dispatchers.IO).launch {
                    userDataStore.data.collect {
                        if (it == UserData.DEFAULT) {
                            emitter.onError(NoSuchElementException("Data is not yet assigned!"))
                        } else {
                            emitter.onSuccess(it)
                        }
                    }
                }
            }.subscribeOn(Schedulers.io())
        }

        fun updateUserData(userData: UserData) {
            CoroutineScope(Dispatchers.IO).launch {
                userDataStore.updateData { userData }
            }
        }

        fun clearUserData() {
            updateUserData(UserData.DEFAULT)
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
    }

    inner class PhotoLoader {
        fun getPhoto(uri: String, compressionFactor: Int = 100): Single<Bitmap> {
            if (photoCacheManager.isPhotoCached(uri)) {
                return Single.just(photoCacheManager.getPhotoFromCache(uri)!!)
                    .subscribeOn(Schedulers.io())
            }
            return LoadUtils.loadPictureFromUrl(uri, compressionFactor)
                .doOnSuccess {
                    try {
                        photoCacheManager.savePhotoToCache(uri, it)
                    } catch (e: Exception) {
                        // Ignore any exceptions to prevent from onError callback
                        Log.e(TAG, "getPhoto: error while saving to cache!", e)
                    }
                }
                .subscribeOn(Schedulers.io())
        }
    }

    companion object {
        private const val TAG = "Main Repository"
    }
}