package ru.flocator.feature_settings.internal.view_models

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import ru.flocator.core_api.api.AppRepository
import ru.flocator.core_data_store.user.info.UserInfo
import ru.flocator.feature_settings.internal.repository.SettingsRepository
import javax.inject.Inject

internal class SettingsFragmentViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val appRepository: AppRepository
) : ViewModel() {
    private val _userInfoLiveData: MutableLiveData<UserInfo?> = MutableLiveData(null)
    val userInfoLiveData: LiveData<UserInfo?>
        get() = _userInfoLiveData

    private val _userAvatarLiveData: MutableLiveData<Bitmap?> = MutableLiveData(null)
    val userAvatarLiveData: LiveData<Bitmap?>
        get() = _userAvatarLiveData

    private val _blackListCountLiveData: MutableLiveData<Int?> = MutableLiveData(null)
    val blackListCountLiveData: LiveData<Int?>
        get() = _blackListCountLiveData

    private val _errorLiveData: MutableLiveData<Throwable?> = MutableLiveData(null)
    val errorLiveData: LiveData<Throwable?>
        get() = _errorLiveData

    private val compositeDisposable = CompositeDisposable()

    fun loadUserAvatar(avatarUri: String) {
        compositeDisposable.add(
            appRepository.photoLoader.getPhoto(avatarUri, AVATAR_QUALITY_FACTOR)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        _userAvatarLiveData.value = it
                    },
                    {
                        _errorLiveData.value = it
                        Log.e(TAG, "loadUserAvatar: error occured", it)
                    }
                )
        )
    }

    fun requestBlacklistCount() {
        compositeDisposable.add(
            settingsRepository.getCurrentUserBlocked()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        _blackListCountLiveData.value = it.size
                    },
                    {
                        _errorLiveData.value = it
                        Log.e(TAG, "Getting blacklist size error", it)
                    }
                )
        )
    }

    fun requestUserInfoData() {
        requestUserInfoFromCache()
        compositeDisposable.add(
            settingsRepository.getCurrentUserInfo()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        _userInfoLiveData.value = it
                    },
                    {
                        _errorLiveData.value = it
                        Log.e(TAG, "Getting UserInfo network data error", it)
                    }
                )
        )
    }

    private fun requestUserInfoFromCache() {
        compositeDisposable.add(
            appRepository.userInfoCache.getUserInfo()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (_userInfoLiveData.value == null) {
                        _userInfoLiveData.value = it
                    }
                }, {
                    Log.e(TAG, "Getting UserInfo cache error:", it)
                })
        )
    }

    companion object {
        const val TAG = "SettingsFragmentViewModelClass"
        const val AVATAR_QUALITY_FACTOR = 30
    }
}