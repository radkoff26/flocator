package com.example.flocator.common.repository

import android.net.Uri
import android.util.Log
import androidx.datastore.core.DataStore
import com.example.flocator.common.storage.db.ApplicationDatabase
import com.example.flocator.common.storage.db.entities.Mark
import com.example.flocator.common.storage.db.entities.MarkPhoto
import com.example.flocator.common.storage.db.entities.MarkWithPhotos
import com.example.flocator.common.storage.db.entities.User
import com.example.flocator.common.storage.storage.point.UserLocationPoint
import com.example.flocator.common.storage.storage.user.UserData
import com.example.flocator.main.api.ClientAPI
import com.example.flocator.main.api.GeocoderAPI
import com.example.flocator.main.data.response.AddressResponse
import com.example.flocator.main.models.dto.MarkDto
import com.example.flocator.main.models.dto.UserLocationDto
import com.example.flocator.main.ui.add_mark.data.AddMarkDto
import com.example.flocator.main.ui.main.data.UserInfo
import com.google.gson.Gson
import com.yandex.mapkit.geometry.Point
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MainRepository @Inject constructor(
    private val clientAPI: ClientAPI,
    private val geocoderAPI: GeocoderAPI,
    private val applicationDatabase: ApplicationDatabase,
    private val userLocationDataStore: DataStore<UserLocationPoint>,
    private val userInfoDataStore: DataStore<UserData>
) {
    val restApi = RestApi()
    val cacheDatabase = CacheDatabase()
    val userCache = UserCache()
    val locationCache = LocationCache()

    inner class RestApi {
        fun getAllFriendsOfUser(userId: Long): Observable<List<User>> {
            val compositeDisposable = CompositeDisposable()
            return Observable.create { emitter ->
                compositeDisposable.addAll(
                    applicationDatabase.userDao().getAllFriends()
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .subscribe(
                            {
                                emitter.onNext(it)
                            },
                            {
                                Log.e(
                                    TAG,
                                    "getAllFriendsOfUser: error while fetching cache database!",
                                    it
                                )
                            }
                        ),
                    clientAPI.getUserFriendsLocated(userId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .subscribe(
                            {
                                emitter.onNext(it)
                                compositeDisposable.add(
                                    cacheDatabase.updateFriends(it)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(Schedulers.io())
                                        .doOnComplete { emitter.onComplete() }
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
            }.subscribeOn(Schedulers.io()).doOnDispose { compositeDisposable.dispose() }
        }

        fun getMarksForUser(userId: Long): Observable<List<MarkWithPhotos>> {
            val compositeDisposable = CompositeDisposable()
            return Observable.create { emitter ->
                compositeDisposable.addAll(
                    applicationDatabase.markDao().getAllMarks()
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .subscribe(
                            {
                                emitter.onNext(it)
                            },
                            {
                                Log.e(
                                    TAG,
                                    "getMarksForUser: error while fetching marks from cache database!",
                                    it
                                )
                            }
                        ),
                    clientAPI.getUserAndFriendsMarks(userId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .subscribe(
                            {
                                val marks = it.map(MarkDto::toMarkWithPhotos)
                                emitter.onNext(marks)
                                val photos = marks.map(MarkWithPhotos::photos).flatten()
                                Completable.concatArray(
                                    cacheDatabase.updateMarks(marks.map(MarkWithPhotos::mark)),
                                    cacheDatabase.updateMarkPhotos(photos)
                                )
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(Schedulers.io())
                                    .doOnComplete { emitter.onComplete() }
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
            }
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

        fun getUserInfo(userId: Long): Single<UserInfo> {
            return clientAPI.getUser(userId).subscribeOn(Schedulers.io())
        }

        fun postUserLocation(userId: Long, location: Point): Completable {
            return clientAPI.updateLocation(
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
                }
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

        fun getCurrentUserData(): Single<UserInfo> {
            return userCache.getUserData()
                .flatMap {
                    getUserInfo(it.userId)
                }
                .subscribeOn(Schedulers.io())
        }
    }

    inner class CacheDatabase {
        fun updateMarks(marks: List<Mark>): Completable {
            return applicationDatabase.markDao().updateTable(marks)
        }

        fun updateMarkPhotos(markPhotos: List<MarkPhoto>): Completable {
            return applicationDatabase.markPhotoDao().updateTable(markPhotos)
        }

        fun updateFriends(friends: List<User>): Completable {
            return applicationDatabase.userDao().updateTable(friends)
        }
    }

    inner class UserCache {
        fun getUserData(): Single<UserData> {
            return Single.create { emitter ->
                CoroutineScope(Dispatchers.IO).launch {
                    userInfoDataStore.data.collect {
                        if (it == UserData.DEFAULT) {
                            emitter.onError(NoSuchElementException("Data is not yet assigned!"))
                        } else {
                            emitter.onSuccess(it)
                        }
                    }
                }
            }
        }

        fun updateUserData(userData: UserData) {
            CoroutineScope(Dispatchers.IO).launch {
                userInfoDataStore.updateData { userData }
            }
        }

        fun clearUserData() {
            updateUserData(UserData.DEFAULT)
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
            }
        }

        fun updateUserLocationData(userLocationPoint: UserLocationPoint) {
            CoroutineScope(Dispatchers.IO).launch {
                userLocationDataStore.updateData { userLocationPoint }
            }
        }
    }

    companion object {
        private const val TAG = "Main Repository"
    }
}