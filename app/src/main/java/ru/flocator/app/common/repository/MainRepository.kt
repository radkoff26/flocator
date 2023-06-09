package ru.flocator.app.common.repository

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.datastore.core.DataStore
import ru.flocator.app.common.cache.global.PhotoCacheManager
import ru.flocator.app.common.connection.ConnectionWrapper
import ru.flocator.app.common.connection.live_data.ConnectionLiveData
import ru.flocator.app.common.storage.db.ApplicationDatabase
import ru.flocator.app.common.storage.db.entities.Mark
import ru.flocator.app.common.storage.db.entities.MarkPhoto
import ru.flocator.app.common.storage.db.entities.MarkWithPhotos
import ru.flocator.app.common.storage.db.entities.User
import ru.flocator.app.common.storage.store.point.UserLocationPoint
import ru.flocator.app.common.storage.store.user.data.UserCredentials
import ru.flocator.app.common.storage.store.user.info.UserInfo
import ru.flocator.app.common.utils.LoadUtils
import ru.flocator.app.community.api.UserApi
import ru.flocator.app.main.data_source.ClientAPI
import ru.flocator.app.main.data_source.GeocoderAPI
import ru.flocator.app.main.domain.address.AddressResponse
import ru.flocator.app.common.dto.mark.MarkDto
import ru.flocator.app.common.dto.location.UserLocationDto
import ru.flocator.app.common.dto.user_name.UsernameDto
import ru.flocator.app.add_mark.domain.dto.AddMarkDto
import ru.flocator.app.settings.data_source.SettingsAPI
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
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
import java.util.stream.Collectors
import javax.inject.Inject
import javax.inject.Singleton

private typealias LoadingImage = Pair<String, Int>

@Singleton
class MainRepository @Inject constructor(
    private val clientAPI: ClientAPI,
    private val geocoderAPI: GeocoderAPI,
    private val userApi: UserApi,
    private val applicationDatabase: ApplicationDatabase,
    private val userLocationDataStore: DataStore<UserLocationPoint>,
    private val userCredentialsStore: DataStore<UserCredentials>,
    private val userInfoStore: DataStore<UserInfo>,
    private val photoCacheManager: PhotoCacheManager,
    private val settingsAPI: SettingsAPI,
    private val connectionLiveData: ConnectionLiveData
) {
    val restApi = RestApi()
    val cacheDatabase = CacheDatabase()
    val userCredentialsCache = UserCredentialsCache()
    val userInfoCache = UserInfoCache()
    val locationCache = LocationCache()
    val photoLoader = PhotoLoader()

    inner class RestApi {
        /**
         * Получение всех друзей юзера
         * */
        fun getAllFriendsOfUser(userId: Long): Single<List<User>> {
            val compositeDisposable = CompositeDisposable()
            return Single.create { emitter ->
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
                        connectionLiveData
                    )
                        .connect()
                        .subscribe(
                            {
                                val marks = it.map(MarkDto::toMarkWithPhotos)
                                emitter.onSuccess(marks)
                                val photos = marks.map(MarkWithPhotos::photos).flatten()
                                compositeDisposable.add(
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

        fun getAddress(latLng: LatLng): Single<String> {
            return geocoderAPI.getAddress("${latLng.latitude}, ${latLng.longitude}")
                .map(AddressResponse::address)
                .subscribeOn(Schedulers.io())
        }

        fun getCurrentUserInfo(): Single<UserInfo> {
            return userCredentialsCache.getUserCredentials()
                .flatMap {
                    getUser(it.userId)
                        .subscribeOn(Schedulers.io())
                }
                .subscribeOn(Schedulers.io())
        }

        fun addFriendByLogin(userId: Long, login: String): Completable {
            return userApi.addNewFriendByLogin(userId, login).subscribeOn(Schedulers.io())
        }

        fun rejectNewFriend(userId: Long, friendId: Long): Completable {
            return userApi.rejectNewFriend(userId, friendId).subscribeOn(Schedulers.io())
        }

        fun acceptNewFriend(userId: Long, friendId: Long): Completable {
            return userApi.acceptNewFriend(userId, friendId).subscribeOn(Schedulers.io())
        }

        fun addNewFriendByBtn(userId: Long, friendId: Long): Completable{
            return userApi.addNewFriend(userId, friendId).subscribeOn(Schedulers.io())
        }

        fun deleteFriendByBtn(userId: Long, friendId: Long): Completable{
            return userApi.deleteFriend(userId, friendId).subscribeOn(Schedulers.io())
        }

        fun blockUserByBtn(blockerId: Long, blockedId: Long): Completable{
            return userApi.blockUser(blockerId, blockedId).subscribeOn(Schedulers.io())
        }

        fun unblockUserByBtn(blockerId: Long, blockedId: Long): Completable{
            return userApi.unblockUser(blockerId, blockedId).subscribeOn(Schedulers.io())
        }

        fun checkLogin(login: String): Single<Boolean>{
            return userApi.isLoginAvailable(login).subscribeOn(Schedulers.io())
        }

        fun changeCurrentUserAva(ava: MultipartBody.Part): Single<Boolean> {
            return userCredentialsCache.getUserCredentials().flatMap {
                settingsAPI.changeAvatar(
                    it.userId,
                    ava
                )
                    .subscribeOn(Schedulers.io())
            }
                .subscribeOn(Schedulers.io())
        }

        fun changeCurrentUserBirthdate(date: Timestamp): Single<Boolean> {
            return userCredentialsCache.getUserCredentials().flatMap {
                settingsAPI.setBirthDate(
                    it.userId,
                    date
                )
                    .subscribeOn(Schedulers.io())
            }
                .subscribeOn(Schedulers.io())
        }

        fun changeCurrentUserName(firstName: String, lastName: String): Single<Boolean> {
            return userCredentialsCache.getUserCredentials().flatMap {
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
            return userCredentialsCache.getUserCredentials().flatMap {
                settingsAPI.changePassword(
                    it.userId,
                    prevPass,
                    pass
                )
                    .observeOn(Schedulers.io())
            }
                .observeOn(Schedulers.io())
        }

        fun getCurrentUserBlocked(): Single<List<UserInfo>> {
            return userCredentialsCache.getUserCredentials().flatMap {
                settingsAPI.getBlocked(
                    it.userId
                )
                    .observeOn(Schedulers.io())
            }
                .observeOn(Schedulers.io())
        }

        fun blockUser(userId: Long): Completable {
            return userCredentialsCache.getUserCredentials().flatMapCompletable {
                settingsAPI.blockUser(
                    it.userId,
                    userId
                )
                    .observeOn(Schedulers.io())
            }
                .observeOn(Schedulers.io())
        }

        fun unblockUser(userId: Long): Completable {
            return userCredentialsCache.getUserCredentials().flatMapCompletable {
                settingsAPI.unblockUser(
                    it.userId,
                    userId
                )
                    .observeOn(Schedulers.io())
            }
                .observeOn(Schedulers.io())
        }

        fun getCurrentUserPrivacy(): Single<Map<Long, String>> {
            return userCredentialsCache.getUserCredentials().flatMap {
                settingsAPI.getPrivacyData(it.userId)
            }. map { privacyData ->
                privacyData.parallelStream().collect(
                    Collectors.toMap(
                        {
                            it.id
                        },
                        {
                            it.status
                        }
                    )
                )
            }
        }


        fun changePrivacy(friendId: Long, status: String): Completable {
            return userCredentialsCache.getUserCredentials().flatMapCompletable {
                settingsAPI.changePrivacyData(it.userId, friendId, status)
                    .observeOn(Schedulers.io())
            }
                .observeOn(Schedulers.io())
        }

        fun getFriendsOfCurrentUser(): Single<List<User>> {
            return userCredentialsCache.getUserCredentials().flatMap {
                getAllFriendsOfUser(it.userId)
                    .observeOn(Schedulers.io())
            }
                .observeOn(Schedulers.io())
        }

        fun deleteCurrentAccount(pass: String): Completable {
            return userCredentialsCache.getUserCredentials().flatMapCompletable {
                settingsAPI.deleteAccount(it.userId, pass)
                    .observeOn(Schedulers.io())
            }
                .observeOn(Schedulers.io())
        }


        fun goOnline(userId: Long): Completable {
            return clientAPI.goOnline(userId).subscribeOn(Schedulers.io())
        }

        fun goOffline(userId: Long): Completable {
            return clientAPI.goOffline(userId).subscribeOn(Schedulers.io())
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