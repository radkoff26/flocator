package ru.flocator.feature_settings.internal.view_models

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import ru.flocator.core.cache.runtime.PhotoCacheLiveData
import ru.flocator.feature_settings.internal.data.friend.BlackListUser
import ru.flocator.feature_settings.internal.data.state.FragmentState
import ru.flocator.feature_settings.internal.exceptions.FailedActionException
import ru.flocator.feature_settings.internal.repository.SettingsRepository
import javax.inject.Inject

internal class BlackListFragmentViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    private val _blockedUsersListLiveData: MutableLiveData<List<BlackListUser>?> =
        MutableLiveData(null)
    val blockedUsersListLiveData: LiveData<List<BlackListUser>?>
        get() = _blockedUsersListLiveData

    private val _fragmentStateLiveData: MutableLiveData<FragmentState> =
        MutableLiveData(FragmentState.LOADING)
    val fragmentStateLiveData: LiveData<FragmentState>
        get() = _fragmentStateLiveData

    private val _errorLiveData: MutableLiveData<Throwable?> =
        MutableLiveData(null)
    val errorLiveData: LiveData<Throwable?>
        get() = _errorLiveData

    private val photoCache: PhotoCacheLiveData = PhotoCacheLiveData(QUALITY_FACTOR)

    private val compositeDisposable = CompositeDisposable()

    fun loadPhoto(uri: String): Single<Bitmap> {
        return photoCache.getPhotoAsync(uri)
    }

    fun requestBlackListLoading() {
        _fragmentStateLiveData.value = FragmentState.LOADING
        compositeDisposable.add(
            settingsRepository.getCurrentUserBlocked()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { userInfoList ->
                        _blockedUsersListLiveData.value = userInfoList.map {
                            BlackListUser(
                                it.userId,
                                it.avatarUri,
                                it.firstName,
                                it.lastName
                            )
                        }.sortedBy { it.userId }
                        _fragmentStateLiveData.value = FragmentState.LOADED
                    },
                    {
                        _errorLiveData.value = it
                        _fragmentStateLiveData.value = FragmentState.FAILED
                        Log.e(TAG, "Getting blacklist error", it)
                    }
                )
        )
    }

    fun unblockUser(userId: Long) {
        val user = removeUserFromList(userId) ?: return
        compositeDisposable.add(
            settingsRepository.unblockUser(userId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {

                    },
                    {
                        _errorLiveData.value = FailedActionException()
                        addUserToList(user)
                        Log.e(
                            TAG,
                            "Error removing from blacklist",
                            it
                        )
                    }
                )
        )
    }

    private fun removeUserFromList(userId: Long): BlackListUser? {
        val prev = _blockedUsersListLiveData.value?.toMutableList() ?: return null
        val indexOfUser = prev.indexOfFirst {
            it.userId == userId
        }
        if (indexOfUser == -1) {
            return null
        }
        val user = prev[indexOfUser]
        prev.removeAt(indexOfUser)
        _blockedUsersListLiveData.value = prev
        _fragmentStateLiveData.value = FragmentState.LOADED
        return user
    }

    private fun addUserToList(user: BlackListUser) {
        val prev = _blockedUsersListLiveData.value?.toMutableList() ?: return
        prev.add(user)
        _blockedUsersListLiveData.value = prev.sortedBy { it.userId }
        _fragmentStateLiveData.value = FragmentState.LOADED
    }

    companion object {
        private const val QUALITY_FACTOR = 30
        const val TAG = "BlackListFragmentViewModelClass"
    }
}