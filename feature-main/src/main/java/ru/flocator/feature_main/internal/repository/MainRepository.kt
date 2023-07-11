package ru.flocator.feature_main.internal.repository

import android.net.Uri
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import ru.flocator.core_api.api.AppRepository
import ru.flocator.core_connection.ConnectionWrapper
import ru.flocator.core_connection.live_data.ConnectionLiveData
import ru.flocator.core_data_store.point.UserLocationPoint
import ru.flocator.core_data_store.user.info.UserInfo
import ru.flocator.core_database.entities.MarkWithPhotos
import ru.flocator.core_database.entities.User
import ru.flocator.core_dto.address.AddressResponse
import ru.flocator.feature_main.internal.data_source.ClientAPI
import ru.flocator.feature_main.internal.data_source.GeocoderAPI
import ru.flocator.feature_main.internal.domain.location.UserLocationDto
import ru.flocator.feature_main.internal.domain.mark.AddMarkDto
import ru.flocator.feature_main.internal.domain.mark.MarkDto
import ru.flocator.feature_main.internal.domain.user_name.UsernameDto
import javax.inject.Inject

internal class MainRepository @Inject constructor(
    private val clientAPI: ClientAPI,
    private val geocoderAPI: GeocoderAPI,
    private val connectionLiveData: ConnectionLiveData,
    private val appRepository: AppRepository
) {
    fun getAllFriendsOfUser(userId: Long): Single<List<User>> {
        return ConnectionWrapper.of(
            clientAPI.getUserFriendsLocated(userId)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io()),
            connectionLiveData
        )
            .connect()
            .subscribeOn(Schedulers.io())
            .doAfterSuccess {
                saveNewFriendsToCache(it)
            }
    }

    private fun saveNewFriendsToCache(newFriends: List<User>) {
        appRepository.cacheDatabase.updateFriends(newFriends)
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
    }

    fun getMarksForUser(userId: Long): Single<List<MarkWithPhotos>> {
        return ConnectionWrapper.of(
            clientAPI.getUserAndFriendsMarks(userId)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io()),
            connectionLiveData
        )
            .connect()
            .subscribeOn(Schedulers.io())
            .doAfterSuccess {
                saveNewMarksToCache(it)
            }.map {
                it.map(MarkDto::toMarkWithPhotos)
            }
    }

    private fun saveNewMarksToCache(newMarks: List<MarkDto>) {
        val marks = newMarks.map(MarkDto::toMarkWithPhotos)
        val photos = marks.map(MarkWithPhotos::photos).flatten()
        Completable.concatArray(
            appRepository.cacheDatabase.updateMarksForUser(
                marks.map(MarkWithPhotos::mark),
                photos
            )
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
                    appRepository.locationCache.updateUserLocationData(
                        UserLocationPoint(
                            location.latitude,
                            location.longitude
                        )
                    )
                },
            connectionLiveData
        ).connect()
    }

    fun getCurrentUserInfo(): Single<UserInfo> {
        return ConnectionWrapper.of(
            appRepository.userCredentialsCache.getUserCredentials()
                .flatMap {
                    getUser(it.userId)
                        .subscribeOn(Schedulers.io())
                },
            connectionLiveData
        ).connect().subscribeOn(Schedulers.io())
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