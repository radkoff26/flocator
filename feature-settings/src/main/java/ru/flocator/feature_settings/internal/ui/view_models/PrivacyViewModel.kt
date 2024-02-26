package ru.flocator.feature_settings.internal.ui.view_models

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import ru.flocator.core.cache.runtime.PhotoCacheLiveData
import ru.flocator.feature_settings.internal.data.model.privacy.PrivacyType
import ru.flocator.feature_settings.internal.data.model.privacy.PrivacyUser
import ru.flocator.feature_settings.internal.data.model.state.FragmentState
import ru.flocator.feature_settings.internal.core.exceptions.FailedActionException
import ru.flocator.feature_settings.internal.data.repository.SettingsRepository
import ru.flocator.feature_settings.internal.data.repository.UserRepository
import javax.inject.Inject

internal class PrivacyViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    private val _privacyListLiveData: MutableLiveData<List<PrivacyUser>?> = MutableLiveData(null)
    val privacyListLiveData: LiveData<List<PrivacyUser>?>
        get() = _privacyListLiveData

    private val _errorLiveData: MutableLiveData<Throwable?> = MutableLiveData(null)
    val errorLiveData: LiveData<Throwable?>
        get() = _errorLiveData

    private val _fragmentStateLiveData: MutableLiveData<FragmentState> =
        MutableLiveData(FragmentState.LOADING)
    val fragmentStateLiveData: LiveData<FragmentState>
        get() = _fragmentStateLiveData

    private var privacyDataMap: Map<Long, PrivacyType>? = null

    private val photoCache = PhotoCacheLiveData(QUALITY_FACTOR)

    private val compositeDisposable = CompositeDisposable()

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }

    fun loadPhoto(uri: String): Single<Bitmap> = photoCache.getPhotoAsync(uri)

    fun loadUserFriendsWithPrivacy() {
        _fragmentStateLiveData.value = FragmentState.LOADING
        compositeDisposable.add(
            userRepository.getFriendsOfCurrentUser()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { friends ->
                        compositeDisposable.add(
                            settingsRepository.getCurrentUserPrivacy()
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(
                                    { privacyDataMap ->
                                        this@PrivacyViewModel.privacyDataMap =
                                            privacyDataMap
                                        _privacyListLiveData.value = friends.map {
                                            PrivacyUser(
                                                it.userId,
                                                it.avatarUri,
                                                it.firstName,
                                                it.lastName,
                                                privacyDataMap[it.userId]!! == PrivacyType.FIXED
                                            )
                                        }
                                        _fragmentStateLiveData.value = FragmentState.LOADED
                                    },
                                    {
                                        _errorLiveData.value = it
                                        _fragmentStateLiveData.value = FragmentState.FAILED
                                        Log.e(TAG, "Getting friends privacy error", it)
                                    }
                                )
                        )
                    },
                    {
                        _errorLiveData.value = it
                        _fragmentStateLiveData.value = FragmentState.FAILED
                        Log.e(TAG, "Getting friends error", it)
                    }
                )
        )
    }

    // Selects all in case when not everything is selected yet
    // Otherwise, uns
    fun selectOrUnselectAll() {
        val list = _privacyListLiveData.value ?: return
        val isAllSelected = list.all {
            it.isChecked
        }
        if (isAllSelected) {
            list.forEach {
                toggleUserPrivacy(it.userId)
            }
        } else {
            list.forEach {
                if (!it.isChecked) {
                    toggleUserPrivacy(it.userId)
                }
            }
        }
    }

    fun toggleUserPrivacy(userId: Long) {
        val map = privacyDataMap ?: return
        val userPrivacyType = map[userId] ?: return
        val toggleResult = toggleUserPrivacyAndUpdateList(userId)
        if (!toggleResult) {
            return
        }
        compositeDisposable.add(
            settingsRepository.changePrivacy(userId, userPrivacyType)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError {
                    _errorLiveData.value = FailedActionException()
                    toggleUserPrivacy(userId)
                    Log.e(TAG, "toggleUserPrivacy: something went wrong", it)
                }
                .subscribe()
        )
    }

    // Returns outcome of made operation:
    // false if some inconsistency occurred
    // true if succeeded
    private fun toggleUserPrivacyAndUpdateList(userId: Long): Boolean {
        val list = _privacyListLiveData.value ?: return false
        val userIndex = list.indexOfFirst { it.userId == userId }
        if (userIndex == -1) {
            return false
        }
        val user = list[userIndex]
        user.isChecked = !user.isChecked
        _privacyListLiveData.value = list
        return true
    }

    companion object {
        private const val QUALITY_FACTOR = 30
        const val TAG = "PrivacyFragmentViewModelClass"
    }
}