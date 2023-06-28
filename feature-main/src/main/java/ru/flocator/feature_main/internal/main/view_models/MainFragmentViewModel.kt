package ru.flocator.feature_main.internal.main.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import ru.flocator.core_api.api.MainRepository
import ru.flocator.core_database.entities.MarkWithPhotos
import ru.flocator.core_database.entities.User
import ru.flocator.core_exceptions.LostConnectionException
import ru.flocator.core_polling.PollingEmitter
import javax.inject.Inject

@Suppress("UNCHECKED_CAST")
internal class MainFragmentViewModel @Inject constructor(
    private val repository: ru.flocator.feature_main.internal.repository.MainRepository,
    private val mainRepository: MainRepository
) : ViewModel() {
    private val _friendsLiveData = MutableLiveData<List<User>>(emptyList())
    val friendsLiveData: LiveData<List<User>>
        get() = _friendsLiveData

    private val _userLocationLiveData = MutableLiveData<LatLng?>(null)
    val userLocationLiveData: LiveData<LatLng?>
        get() = _userLocationLiveData

    private val _userInfoLiveData: MutableLiveData<ru.flocator.core_data_store.user.info.UserInfo?> =
        MutableLiveData(null)
    val userInfoLiveData: LiveData<ru.flocator.core_data_store.user.info.UserInfo?>
        get() = _userInfoLiveData

    private val _marksLiveData: MutableLiveData<List<MarkWithPhotos>> = MutableLiveData(emptyList())
    val marksLiveData: LiveData<List<MarkWithPhotos>>
        get() = _marksLiveData

    private val compositeDisposable = CompositeDisposable()

    private fun initialFetch() {
        val userId = userInfoLiveData.value!!.userId
        compositeDisposable.addAll(
            repository.getAllFriendsOfUser(userId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        updateFriends(it)
                    },
                    {
                        Log.e(TAG, "Initialization: marks loading failed!", it)
                    }
                ),
            repository.getMarksForUser(userId)
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

    fun loadPhoto(uri: String) = mainRepository.photoLoader.getPhoto(uri, COMPRESSION_FACTOR)

    private fun retrieveDataFromCache() {
        compositeDisposable.addAll(
            mainRepository.userInfoCache.getUserInfo()
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
            mainRepository.locationCache.getUserLocationData()
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
            mainRepository.cacheDatabase.retrieveFriendsFromCache()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        updateFriends(it)
                    },
                    {
                        Log.e(TAG, "retrieveDataFromCache: error while loading friends cache!", it)
                    }
                ),
            mainRepository.cacheDatabase.retrieveMarksFromCache()
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
            repository.getCurrentUserInfo()
                .observeOn(AndroidSchedulers.mainThread())
                .retry(TIMES_TO_RETRY_INITIAL_FETCHING.toLong()) { throwable ->
                    throwable is LostConnectionException
                }
                .subscribe(
                    {
                        _userInfoLiveData.value = it
                        mainRepository.userInfoCache.updateUserInfo(it)
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
            repository.getCurrentUserInfo()
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
            repository.postUserLocation(
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
        mainRepository.locationCache.updateUserLocationData(
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
            repository.getAllFriendsOfUser(userInfoLiveData.value!!.userId)
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
            repository.getMarksForUser(userInfoLiveData.value!!.userId)
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
        repository.goOffline(userInfoLiveData.value!!.userId).subscribe()
    }

    fun goOnlineAsUser() {
        if (userInfoLiveData.value == null) {
            return
        }
        repository.goOnline(userInfoLiveData.value!!.userId).subscribe()
    }

    private fun updateFriends(users: List<User>) {
        _friendsLiveData.value = users
    }

    private fun updateMarks(value: List<MarkWithPhotos>) {
        _marksLiveData.value = value
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