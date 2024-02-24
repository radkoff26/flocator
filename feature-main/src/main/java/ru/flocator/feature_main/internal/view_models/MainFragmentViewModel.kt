package ru.flocator.feature_main.internal.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import ru.flocator.core.cache.global.PhotoLoader
import ru.flocator.core.exceptions.LostConnectionException
import ru.flocator.core.polling.PollingEmitter
import ru.flocator.data.data_store.info.UserInfo
import ru.flocator.data.data_store.info.UserInfoDataStoreManager
import ru.flocator.data.data_store.point.UserLocationDataStoreManager
import ru.flocator.data.data_store.point.UserLocationPoint
import ru.flocator.data.database.ApplicationDatabase
import ru.flocator.data.database.entities.MarkWithPhotos
import ru.flocator.data.database.entities.User
import ru.flocator.data.models.location.Coordinates
import ru.flocator.feature_main.internal.repository.MainRepository
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@Suppress("UNCHECKED_CAST")
internal class MainFragmentViewModel @Inject constructor(
    private val mainRepository: MainRepository,
    private val locationDataStoreManager: UserLocationDataStoreManager,
    private val infoDataStoreManager: UserInfoDataStoreManager,
    private val photoLoader: PhotoLoader,
    private val database: ApplicationDatabase
) : ViewModel() {
    private val _friendsLiveData = MutableLiveData<Map<Long, User>>(emptyMap())
    val friendsLiveData: LiveData<Map<Long, User>>
        get() = _friendsLiveData

    private val _userLocationLiveData = MutableLiveData<Coordinates?>(null)
    val userLocationLiveData: LiveData<Coordinates?>
        get() = _userLocationLiveData

    private val _userInfoLiveData: MutableLiveData<UserInfo?> =
        MutableLiveData(null)
    val userInfoLiveData: LiveData<UserInfo?>
        get() = _userInfoLiveData

    private val _marksLiveData: MutableLiveData<Map<Long, MarkWithPhotos>> =
        MutableLiveData(emptyMap())
    val marksLiveData: LiveData<Map<Long, MarkWithPhotos>>
        get() = _marksLiveData

    private val _errorLiveData: MutableLiveData<Throwable?> = MutableLiveData(null)
    val errorLiveData: LiveData<Throwable?>
        get() = _errorLiveData

    val isCameraInitialized: AtomicBoolean = AtomicBoolean()

    private val compositeDisposable = CompositeDisposable()

    private fun initialFetch() {
        val userId = userInfoLiveData.value!!.userId
        compositeDisposable.addAll(
            mainRepository.getAllFriendsOfUser(userId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { users ->
                        updateFriends(
                            users.map { // TODO: here and everywhere - eliminate this mapping overhead
                                User(
                                    it.userId,
                                    it.firstName,
                                    it.lastName,
                                    it.location,
                                    it.avatarUri
                                )
                            }
                        )
                    },
                    {
                        _errorLiveData.value = it
                        Log.e(TAG, "Initialization: marks loading failed!", it)
                    }
                ),
            mainRepository.getMarksForUser(userId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        updateMarks(it)
                    },
                    {
                        _errorLiveData.value = it
                        Log.e(TAG, "Initialization: friends loading failed!", it)
                    }
                )
        )
    }

    fun loadPhoto(uri: String) = photoLoader.getPhoto(uri, COMPRESSION_FACTOR)

    private fun retrieveMapDataFromCache() {
        compositeDisposable.addAll(
            infoDataStoreManager.getUserInfo()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        if (_userInfoLiveData.value == null) {
                            _userInfoLiveData.value = it
                        }
                    },
                    {
                        Log.e(
                            TAG,
                            "initialFetch: error while fetching user info from cache",
                            it
                        )
                    }
                ),
            locationDataStoreManager.getUserLocation()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        if (_userLocationLiveData.value == null) {
                            _userLocationLiveData.value = Coordinates(
                                it.latitude,
                                it.longitude
                            )
                        }
                    },
                    {
                        Log.e(
                            TAG,
                            "initialFetch: error while fetching user location from cache",
                            it
                        )
                    }
                ),
            database.userDao().getAllFriends()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { users ->
                        if (_friendsLiveData.value!!.isEmpty()) {
                            updateFriends(
                                users.map {
                                    User(
                                        it.userId,
                                        it.firstName,
                                        it.lastName,
                                        it.location,
                                        it.avatarUri
                                    )
                                }
                            )
                        }
                    },
                    {
                        Log.e(TAG, "retrieveDataFromCache: error while loading friends cache!", it)
                    }
                ),
            database.markDao().getAllMarks()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        if (_marksLiveData.value!!.isEmpty()) {
                            updateMarks(it)
                        }
                    },
                    {
                        Log.e(TAG, "retrieveDataFromCache: error while loading marks cache!", it)
                    }
                ),
        )
    }

    fun requestInitialLoading() {
        retrieveMapDataFromCache()
        compositeDisposable.add(
            mainRepository.getCurrentUserInfo()
                .observeOn(AndroidSchedulers.mainThread())
                .retry(TIMES_TO_RETRY_INITIAL_FETCHING) { throwable ->
                    throwable is LostConnectionException
                }
                .subscribe(
                    {
                        _userInfoLiveData.value = it
                        infoDataStoreManager.setUserInfo(it)
                        initialFetch()
                    },
                    {
                        Log.e(
                            TAG,
                            "requestUserData: ${it.stackTraceToString()}",
                            it
                        )
                    }
                )
        )
    }

    fun fetchUserInfo(emitter: PollingEmitter) {
        compositeDisposable.add(
            mainRepository.getCurrentUserInfo()
                .observeOn(AndroidSchedulers.mainThread())
                .retry(TIMES_TO_RETRY_USER_INFO_FETCHING) {
                    it is LostConnectionException
                }
                .subscribe(
                    {
                        _userInfoLiveData.value = it
                        emitter.emit()
                    },
                    {
                        Log.e(
                            TAG,
                            "requestUserData: ${it.stackTraceToString()}",
                            it
                        )
                        emitter.emit()
                    }
                )
        )
    }

    fun postLocation() {
        if (_userInfoLiveData.value == null || _userLocationLiveData.value == null) {
            return
        }
        compositeDisposable.add(
            mainRepository.postUserLocation(
                _userInfoLiveData.value!!.userId,
                _userLocationLiveData.value!!
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .retry(TIMES_TO_RETRY_LOCATION_POST) {
                    it is LostConnectionException
                }
                .doOnError {
                    Log.e(TAG, "postLocation: error", it)
                }
                .subscribe()
        )
    }

    fun updateUserLocation(location: LatLng) {
        _userLocationLiveData.value = Coordinates(
            location.latitude,
            location.longitude
        )
        locationDataStoreManager.setUserLocation(
            UserLocationPoint(
                location.latitude,
                location.longitude
            )
        )
    }

    override fun onCleared() {
        super.onCleared()
        clearError()
        compositeDisposable.dispose()
    }

    fun fetchFriends(emitter: PollingEmitter) {
        if (userInfoLiveData.value == null) {
            emitter.emit()
            return
        }
        compositeDisposable.add(
            mainRepository.getAllFriendsOfUser(userInfoLiveData.value!!.userId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .retry(TIMES_TO_RETRY_FRIENDS_FETCHING) {
                    it is LostConnectionException
                }
                .subscribe(
                    { users ->
                        updateFriends(
                            users.map {
                                User(
                                    it.userId,
                                    it.firstName,
                                    it.lastName,
                                    it.location,
                                    it.avatarUri
                                )
                            }
                        )
                        emitter.emit()
                    },
                    {
                        _errorLiveData.value = it
                        Log.e(TAG, "Failed while loading friends!", it)
                        emitter.emit()
                    }
                )
        )
    }

    fun fetchMarks(emitter: PollingEmitter) {
        if (userInfoLiveData.value == null) {
            emitter.emit()
            return
        }
        compositeDisposable.add(
            mainRepository.getMarksForUser(userInfoLiveData.value!!.userId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .retry(TIMES_TO_RETRY_MARKS_FETCHING) {
                    it is LostConnectionException
                }
                .subscribe(
                    {
                        updateMarks(it)
                        emitter.emit()
                    },
                    {
                        _errorLiveData.value = it
                        Log.e(TAG, "Failed while loading marks!", it)
                        emitter.emit()
                    }
                )
        )
    }

    fun goOfflineAsUser() {
        if (userInfoLiveData.value == null) {
            return
        }
        mainRepository.goOffline(userInfoLiveData.value!!.userId).subscribe()
    }

    fun goOnlineAsUser() {
        if (userInfoLiveData.value == null) {
            return
        }
        mainRepository.goOnline(userInfoLiveData.value!!.userId).subscribe()
    }

    fun clearError() {
        _errorLiveData.value = null
    }

    private fun updateFriends(users: List<User>) {
        _friendsLiveData.value = buildMap {
            users.forEach {
                put(it.userId, it)
            }
        }
    }

    private fun updateMarks(marks: List<MarkWithPhotos>) {
        _marksLiveData.value = buildMap {
            marks.forEach {
                put(it.mark.markId, it)
            }
        }
    }

    companion object {
        const val TAG = "Main Fragment View Model"
        const val COMPRESSION_FACTOR = 10
        const val TIMES_TO_RETRY_LOCATION_POST = 5L
        const val TIMES_TO_RETRY_FRIENDS_FETCHING = 10L
        const val TIMES_TO_RETRY_MARKS_FETCHING = 7L
        const val TIMES_TO_RETRY_INITIAL_FETCHING = 5L
        const val TIMES_TO_RETRY_USER_INFO_FETCHING = 5L
    }
}