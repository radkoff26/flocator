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
import ru.flocator.core.connection.ConnectionWrapper
import ru.flocator.core.connection.live_data.ConnectionLiveData
import ru.flocator.data.data_store.credentials.UserCredentialsDataStoreManager
import ru.flocator.data.data_store.info.UserInfo
import ru.flocator.data.data_store.point.UserLocationDataStoreManager
import ru.flocator.data.data_store.point.UserLocationPoint
import ru.flocator.data.database.ApplicationDatabase
import ru.flocator.data.database.entities.MarkWithPhotos
import ru.flocator.data.database.entities.User
import ru.flocator.data.models.address.AddressResponse
import ru.flocator.data.models.location.Coordinates
import ru.flocator.feature_main.internal.data.location.UserLocationDto
import ru.flocator.feature_main.internal.data.mark.AddMarkDto
import ru.flocator.feature_main.internal.data.mark.MarkDto
import ru.flocator.feature_main.internal.data.user_name.UsernameDto
import ru.flocator.feature_main.internal.data_source.GeocoderDataSource
import ru.flocator.feature_main.internal.data_source.MainDataSource
import javax.inject.Inject

internal class MainRepository @Inject constructor(
    private val mainDataSource: MainDataSource,
    private val geocoderDataSource: GeocoderDataSource,
    private val connectionLiveData: ConnectionLiveData,
    private val database: ApplicationDatabase,
    private val locationDataStoreManager: UserLocationDataStoreManager,
    private val credentialsDataStoreManager: UserCredentialsDataStoreManager
) {
    fun getAllFriendsOfUser(userId: Long): Single<List<User>> {
        return ConnectionWrapper.of(
            mainDataSource.getUserFriendsLocated(userId)
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
        database.userDao().updateTable(newFriends)
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
            mainDataSource.getUserAndFriendsMarks(userId)
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
            database.updateMarks(
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
        return mainDataSource.postMark(addMarkDtoToPost, photosToPost).subscribeOn(Schedulers.io())
    }

    fun getMark(markId: Long, userId: Long): Single<MarkDto> {
        return mainDataSource.getMark(markId, userId).subscribeOn(Schedulers.io())
    }

    fun getUser(userId: Long): Single<UserInfo> {
        return mainDataSource.getUser(userId).subscribeOn(Schedulers.io())
    }

    fun getUsername(userId: Long): Single<UsernameDto> {
        return mainDataSource.getUsername(userId).subscribeOn(Schedulers.io())
    }

    fun postUserLocation(userId: Long, location: Coordinates): Completable {
        return ConnectionWrapper.of(
            mainDataSource.updateLocation(
                UserLocationDto(
                    userId,
                    location
                )
            )
                .subscribeOn(Schedulers.io())
                .doOnComplete {
                    locationDataStoreManager.setUserLocation(
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
            credentialsDataStoreManager.getUserCredentials()
                .flatMap {
                    getUser(it.userId)
                        .subscribeOn(Schedulers.io())
                },
            connectionLiveData
        ).connect().subscribeOn(Schedulers.io())
    }

    fun likeMark(markId: Long, userId: Long): Completable {
        return mainDataSource.likeMark(markId, userId).subscribeOn(Schedulers.io())
    }

    fun unlikeMark(markId: Long, userId: Long): Completable {
        return mainDataSource.unlikeMark(markId, userId).subscribeOn(Schedulers.io())
    }

    fun getAddress(latLng: LatLng): Single<String> {
        return geocoderDataSource.getAddress("${latLng.latitude}, ${latLng.longitude}")
            .map(AddressResponse::address)
            .subscribeOn(Schedulers.io())
    }

    fun goOnline(userId: Long): Completable {
        return mainDataSource.goOnline(userId).subscribeOn(Schedulers.io())
    }

    fun goOffline(userId: Long): Completable {
        return mainDataSource.goOffline(userId).subscribeOn(Schedulers.io())
    }


    companion object {
        private const val TAG = "Main Repository"
    }
}