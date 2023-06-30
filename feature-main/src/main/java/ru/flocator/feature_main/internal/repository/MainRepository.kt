package ru.flocator.feature_main.internal.repository

import android.net.Uri
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import ru.flocator.core_connection.ConnectionWrapper
import ru.flocator.core_data_store.point.UserLocationPoint
import ru.flocator.core_data_store.user.info.UserInfo
import ru.flocator.core_database.entities.MarkWithPhotos
import ru.flocator.core_database.entities.User
import ru.flocator.core_dto.address.AddressResponse
import ru.flocator.core_dto.location.UserLocationDto
import ru.flocator.core_dto.mark.AddMarkDto
import ru.flocator.core_dto.mark.MarkDto
import ru.flocator.core_dto.user_name.UsernameDto
import ru.flocator.feature_main.api.dependencies.MainDependencies
import ru.flocator.feature_main.internal.data_source.ClientAPI
import ru.flocator.feature_main.internal.data_source.GeocoderAPI
import javax.inject.Inject

internal class MainRepository @Inject constructor(
    private val clientAPI: ClientAPI,
    private val geocoderAPI: GeocoderAPI,
    private val dependencies: MainDependencies
) {
    fun getAllFriendsOfUser(userId: Long): Single<List<User>> {
        val compositeDisposable = CompositeDisposable()
        return Single.create { emitter ->
            compositeDisposable.add(
                ConnectionWrapper.of(
                    clientAPI.getUserFriendsLocated(userId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io()),
                    dependencies.connectionLiveData
                )
                    .connect()
                    .subscribe(
                        {
                            emitter.onSuccess(it)
                            compositeDisposable.add(
                                dependencies.appRepository.cacheDatabase.updateFriends(it)
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
        }
            .subscribeOn(Schedulers.io())
            .doOnDispose { compositeDisposable.dispose() }
    }

    fun getMarksForUser(userId: Long): Single<List<MarkWithPhotos>> {
        val compositeDisposable = CompositeDisposable()
        return Single.create<List<MarkWithPhotos>> { emitter ->
            compositeDisposable.add(
                ConnectionWrapper.of(
                    clientAPI.getUserAndFriendsMarks(userId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io()),
                    dependencies.connectionLiveData
                )
                    .connect()
                    .subscribe(
                        {
                            val marks = it.map(MarkDto::toMarkWithPhotos)
                            emitter.onSuccess(marks)
                            val photos = marks.map(MarkWithPhotos::photos).flatten()
                            compositeDisposable.add(
                                Completable.concatArray(
                                    dependencies.appRepository.cacheDatabase.updateMarks(marks.map(MarkWithPhotos::mark)),
                                    dependencies.appRepository.cacheDatabase.updateMarkPhotos(photos)
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
                            )
                        },
                        {
                            Log.e(
                                TAG,
                                "getMarksForUser: error while fetching marks from server!",
                                it
                            )
                            emitter.onError(it)
                        }
                    ),
            )
        }
            .subscribeOn(Schedulers.io())
            .doOnDispose { compositeDisposable.dispose() }
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

    fun getUsername(userId: Long): Single<UsernameDto> {
        return clientAPI.getUsername(userId).subscribeOn(Schedulers.io())
    }

    fun postUserLocation(userId: Long, location: LatLng): Completable {
        return ConnectionWrapper.of(
            clientAPI.updateLocation(
                UserLocationDto(
                    userId,
                    location
                )
            )
                .subscribeOn(Schedulers.io())
                .doOnComplete {
                    dependencies.appRepository.locationCache.updateUserLocationData(
                        UserLocationPoint(
                            location.latitude,
                            location.longitude
                        )
                    )
                },
            dependencies.connectionLiveData
        ).connect()
    }

    fun getCurrentUserInfo(): Single<UserInfo> {
        return dependencies.appRepository.userCredentialsCache.getUserCredentials()
            .flatMap {
                getUser(it.userId)
                    .subscribeOn(Schedulers.io())
            }
            .subscribeOn(Schedulers.io())
    }

    fun likeMark(markId: Long, userId: Long): Completable {
        return clientAPI.likeMark(markId, userId).subscribeOn(Schedulers.io())
    }

    fun unlikeMark(markId: Long, userId: Long): Completable {
        return clientAPI.unlikeMark(markId, userId).subscribeOn(Schedulers.io())
    }

    fun getAddress(latLng: LatLng): Single<String> {
        return geocoderAPI.getAddress("${latLng.latitude}, ${latLng.longitude}")
            .map(AddressResponse::address)
            .subscribeOn(Schedulers.io())
    }

    fun goOnline(userId: Long): Completable {
        return clientAPI.goOnline(userId).subscribeOn(Schedulers.io())
    }

    fun goOffline(userId: Long): Completable {
        return clientAPI.goOffline(userId).subscribeOn(Schedulers.io())
    }


    companion object {
        private const val TAG = "Main Repository"
    }
}