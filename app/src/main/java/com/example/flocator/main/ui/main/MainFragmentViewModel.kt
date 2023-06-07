package com.example.flocator.main.ui.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.flocator.common.exceptions.LostConnectionException
import com.example.flocator.common.polling.PollingEmitter
import com.example.flocator.common.repository.MainRepository
import com.example.flocator.common.storage.db.entities.MarkWithPhotos
import com.example.flocator.common.storage.db.entities.User
import com.example.flocator.common.storage.store.point.UserLocationPoint
import com.example.flocator.common.storage.store.user.info.UserInfo
import com.example.flocator.main.models.*
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
@Suppress("UNCHECKED_CAST")
class MainFragmentViewModel @Inject constructor(
    private val repository: MainRepository
) : ViewModel() {
    private val _friendsLiveData = MutableLiveData<List<User>>(emptyList())
    val friendsLiveData: LiveData<List<User>>
        get() = _friendsLiveData

    private val _userLocationLiveData = MutableLiveData<LatLng?>(null)
    val userLocationLiveData: LiveData<LatLng?>
        get() = _userLocationLiveData

    private val _userInfoLiveData: MutableLiveData<UserInfo?> = MutableLiveData(null)
    val userInfoLiveData: LiveData<UserInfo?>
        get() = _userInfoLiveData

    private val _marksLiveData: MutableLiveData<List<MarkWithPhotos>> = MutableLiveData(emptyList())
    val marksLiveData: LiveData<List<MarkWithPhotos>>
        get() = _marksLiveData

    private var mapWidth: Float? = null
    private var markWidth: Float? = null

    private val compositeDisposable = CompositeDisposable()

    private fun initialFetch() {
        val userId = userInfoLiveData.value!!.userId
        compositeDisposable.addAll(
            repository.restApi.getAllFriendsOfUser(userId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        updateFriends(it)
                    },
                    {
                        Log.e(TAG, "Initialization: marks loading failed!", it)
                    }
                ),
            repository.restApi.getMarksForUser(userId)
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

    fun loadPhoto(uri: String) = repository.photoLoader.getPhoto(uri, COMPRESSION_FACTOR)

    private fun retrieveDataFromCache() {
        compositeDisposable.addAll(
            repository.userInfoCache.getUserInfo()
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
            repository.locationCache.getUserLocationData()
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
            repository.cacheDatabase.retrieveFriendsFromCache()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        updateFriends(it)
                    },
                    {
                        Log.e(TAG, "retrieveDataFromCache: error while loading friends cache!", it)
                    }
                ),
            repository.cacheDatabase.retrieveMarksFromCache()
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
            repository.restApi.getCurrentUserInfo()
                .observeOn(AndroidSchedulers.mainThread())
                .retry(TIMES_TO_RETRY_INITIAL_FETCHING.toLong()) { throwable ->
                    throwable is LostConnectionException
                }
                .subscribe(
                    {
                        _userInfoLiveData.value = it
                        repository.userInfoCache.updateUserInfo(it)
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
            repository.restApi.getCurrentUserInfo()
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
            repository.restApi.postUserLocation(
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

    fun setWidths(mapWidth: Float, markWidth: Float) {
        this.mapWidth = mapWidth
        this.markWidth = markWidth
    }

    fun updateUserLocation(location: LatLng) {
        _userLocationLiveData.value = location
        repository.locationCache.updateUserLocationData(
            UserLocationPoint(
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
            repository.restApi.getAllFriendsOfUser(userInfoLiveData.value!!.userId)
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
            repository.restApi.getMarksForUser(userInfoLiveData.value!!.userId)
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
        repository.restApi.goOffline(userInfoLiveData.value!!.userId).subscribe()
    }

    fun goOnlineAsUser() {
        if (userInfoLiveData.value == null) {
            return
        }
        repository.restApi.goOnline(userInfoLiveData.value!!.userId).subscribe()
    }

    private fun updateFriends(users: List<User>) {
        _friendsLiveData.value = users
    }

    private fun updateMarks(value: List<MarkWithPhotos>) {
        _marksLiveData.value = value
    }

    companion object {
        const val TAG = "Main Fragment View Model"
        const val COMPRESSION_FACTOR = 20
        const val TIMES_TO_RETRY_LOCATION_POST = 5
        const val TIMES_TO_RETRY_FRIENDS_FETCHING = 10
        const val TIMES_TO_RETRY_MARKS_FETCHING = 7
        const val TIMES_TO_RETRY_INITIAL_FETCHING = 5
        const val TIMES_TO_RETRY_USER_INFO_FETCHING = 5
    }
}