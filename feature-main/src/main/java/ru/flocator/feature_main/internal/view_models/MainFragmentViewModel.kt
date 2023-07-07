package ru.flocator.feature_main.internal.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import ru.flocator.core_api.api.AppRepository
import ru.flocator.core_data_store.user.info.UserInfo
import ru.flocator.core_database.entities.MarkWithPhotos
import ru.flocator.core_database.entities.User
import ru.flocator.core_exceptions.LostConnectionException
import ru.flocator.core_polling.PollingEmitter
import ru.flocator.feature_main.internal.repository.MainRepository
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@Suppress("UNCHECKED_CAST")
internal class MainFragmentViewModel @Inject constructor(
    private val mainRepository: MainRepository,
    private val appRepository: AppRepository
) : ViewModel() {
    private val _friendsLiveData = MutableLiveData<Map<Long, User>>(emptyMap())
    val friendsLiveData: LiveData<Map<Long, User>>
        get() = _friendsLiveData

    private val _userLocationLiveData = MutableLiveData<LatLng?>(null)
    val userLocationLiveData: LiveData<LatLng?>
        get() = _userLocationLiveData

    private val _userInfoLiveData: MutableLiveData<UserInfo?> =
        MutableLiveData(null)
    val userInfoLiveData: LiveData<UserInfo?>
        get() = _userInfoLiveData

    private val _marksLiveData: MutableLiveData<Map<Long, MarkWithPhotos>> =
        MutableLiveData(emptyMap())
    val marksLiveData: LiveData<Map<Long, MarkWithPhotos>>
        get() = _marksLiveData

    val isCameraInitialized: AtomicBoolean = AtomicBoolean()

    private val compositeDisposable = CompositeDisposable()

    private fun initialFetch() {
        val userId = userInfoLiveData.value!!.userId
        compositeDisposable.addAll(
            mainRepository.getAllFriendsOfUser(userId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        updateFriends(it)
                    },
                    {
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
                        Log.e(TAG, "Initialization: friends loading failed!", it)
                    }
                )
        )
    }

    fun loadPhoto(uri: String) = appRepository.photoLoader.getPhoto(uri, COMPRESSION_FACTOR)

    private fun retrieveDataFromCache() {
        compositeDisposable.addAll(
            appRepository.userInfoCache.getUserInfo()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        _userInfoLiveData.value = it
                    },
                    {
                        Log.e(
                            TAG,
                            "initialFetch: error while fetching user info from cache",
                            it
                        )
                    }
                ),
            appRepository.locationCache.getUserLocationData()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        _userLocationLiveData.postValue(
                            LatLng(
                                it.latitude,
                                it.longitude
                            )
                        )
                    },
                    {
                        Log.e(
                            TAG,
                            "initialFetch: error while fetching user location from cache",
                            it
                        )
                    }
                ),
            appRepository.cacheDatabase.retrieveFriendsFromCache()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        updateFriends(it)
                    },
                    {
                        Log.e(TAG, "retrieveDataFromCache: error while loading friends cache!", it)
                    }
                ),
            appRepository.cacheDatabase.retrieveMarksFromCache()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        updateMarks(it)
                    },
                    {
                        Log.e(TAG, "retrieveDataFromCache: error while loading marks cache!", it)
                    }
                ),
        )
    }

    fun requestInitialLoading() {
        retrieveDataFromCache()
        compositeDisposable.add(
            mainRepository.getCurrentUserInfo()
                .observeOn(AndroidSchedulers.mainThread())
                .retry(TIMES_TO_RETRY_INITIAL_FETCHING.toLong()) { throwable ->
                    throwable is LostConnectionException
                }
                .subscribe(
                    {
                        _userInfoLiveData.value = it
                        appRepository.userInfoCache.updateUserInfo(it)
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
                .retry(TIMES_TO_RETRY_USER_INFO_FETCHING.toLong()) {
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
                .retry(TIMES_TO_RETRY_LOCATION_POST.toLong()) {
                    it is LostConnectionException
                }
                .doOnError {
                    Log.e(TAG, "postLocation: error", it)
                }
                .subscribe()
        )
    }

    fun updateUserLocation(location: LatLng) {
        _userLocationLiveData.value = location
        appRepository.locationCache.updateUserLocationData(
            ru.flocator.core_data_store.point.UserLocationPoint(
                location.latitude,
                location.longitude
            )
        )
    }

    override fun onCleared() {
        super.onCleared()
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
                .retry(TIMES_TO_RETRY_FRIENDS_FETCHING.toLong()) {
                    it is LostConnectionException
                }
                .subscribe(
                    {
                        updateFriends(it)
                        emitter.emit()
                    },
                    {
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
                .retry(TIMES_TO_RETRY_MARKS_FETCHING.toLong()) {
                    it is LostConnectionException
                }
                .subscribe(
                    {
                        updateMarks(it)
                        emitter.emit()
                    },
                    {
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

    private fun updateFriends(users: List<User>) {
        _friendsLiveData.value = buildMap {
            users.forEach {
                put(it.id, it)
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
        const val TIMES_TO_RETRY_LOCATION_POST = 5
        const val TIMES_TO_RETRY_FRIENDS_FETCHING = 10
        const val TIMES_TO_RETRY_MARKS_FETCHING = 7
        const val TIMES_TO_RETRY_INITIAL_FETCHING = 5
        const val TIMES_TO_RETRY_USER_INFO_FETCHING = 5
    }
}