package ru.flocator.feature_settings.internal.ui.view_models

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import ru.flocator.core.cache.global.PhotoLoader
import ru.flocator.core.exceptions.UiThrowable
import ru.flocator.data.data_store.info.UserInfo
import ru.flocator.data.data_store.info.UserInfoMediator
import ru.flocator.data.models.language.Language
import ru.flocator.data.preferences.LanguagePreferences
import ru.flocator.feature_settings.R
import ru.flocator.feature_settings.internal.data.repository.SettingsRepository
import ru.flocator.feature_settings.internal.domain.ChangeAvatarPhotoUseCase
import java.sql.Timestamp
import javax.inject.Inject

internal class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val photoLoader: PhotoLoader,
    private val userInfoMediator: UserInfoMediator,
    private val languagePreferences: LanguagePreferences,
    private val changeAvatarPhotoUseCase: ChangeAvatarPhotoUseCase
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

    private val _errorLiveData: MutableLiveData<UiThrowable?> = MutableLiveData(null)
    val errorLiveData: LiveData<UiThrowable?>
        get() = _errorLiveData

    private val compositeDisposable = CompositeDisposable()

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }

    fun changeUserAvatar(imageBytes: ByteArray, uri: String) {
        val photoPart = MultipartBody.Part.createFormData(
            "photo",
            uri,
            RequestBody.create(
                MediaType.parse("image/*"),
                imageBytes
            )
        )
        compositeDisposable.add(
            changeAvatarPhotoUseCase(photoPart)
                .doOnError {
                    _errorLiveData.value = UiThrowable(R.string.failed_to_change_avatar, it)
                }.subscribe()
        )
    }

    fun changeCurrentUserBirthDate(birthDate: Timestamp) {
        compositeDisposable.add(
            settingsRepository.changeCurrentUserBirthdate(birthDate)
                .doOnError {
                    _errorLiveData.value = UiThrowable(R.string.failed_to_change_birth_date, it)
                    Log.e(TAG, "changeCurrentUserBirthDate: failed!", it)
                }
                .subscribe()
        )
    }

    fun changeLanguage(language: Language, onChanged: () -> Unit) {
        languagePreferences.setLanguage(language)
        onChanged()
    }

    fun getLanguage() = languagePreferences.getLanguage()

    fun loadUserAvatar(avatarUri: String) {
        compositeDisposable.add(
            photoLoader.getPhoto(avatarUri, AVATAR_QUALITY_FACTOR)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        _userAvatarLiveData.value = it
                    },
                    {
                        Log.e(TAG, "loadUserAvatar: error occurred!", it)
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
                        Log.e(TAG, "Getting blacklist size error", it)
                    }
                )
        )
    }

    fun requestUserInfoData() {
        compositeDisposable.add(
            userInfoMediator.getUserInfo()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        _userInfoLiveData.value = it
                    },
                    {
                        Log.e(TAG, "Getting UserInfo data error", it)
                    }
                )
        )
    }

    companion object {
        const val TAG = "SettingsFragmentViewModelClass"
        const val AVATAR_QUALITY_FACTOR = 30
    }
}