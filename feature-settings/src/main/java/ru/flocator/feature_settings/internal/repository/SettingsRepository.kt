package ru.flocator.feature_settings.internal.repository

import android.util.Log
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import okhttp3.MultipartBody
import ru.flocator.core.connection.live_data.ConnectionLiveData
import ru.flocator.data.data_store.info.UserInfo
import ru.flocator.data.data_store.info.UserInfoMediator
import ru.flocator.data.data_store.point.UserLocationDataStoreManager
import ru.flocator.data.database.ApplicationDatabase
import ru.flocator.data.database.entities.User
import ru.flocator.data.token.TokenPreferences
import ru.flocator.feature_settings.internal.data.privacy.PrivacyType
import ru.flocator.feature_settings.internal.data_source.SettingsDataSource
import java.sql.Timestamp
import javax.inject.Inject

internal class SettingsRepository @Inject constructor(
    private val settingsDataSource: SettingsDataSource,
    private val locationDataStoreManager: UserLocationDataStoreManager,
    private val userInfoMediator: UserInfoMediator,
    private val tokenPreferences: TokenPreferences
) {

    fun clearCache() {
        userInfoMediator.clearData()
        locationDataStoreManager.clearUserLocation()
        tokenPreferences.clear()
    }

    fun changeCurrentUserAvatar(avatarUri: String): Single<Boolean> =
        settingsDataSource.changeAvatar(avatarUri).subscribeOn(Schedulers.io())

    fun changeCurrentUserBirthdate(date: Timestamp): Single<Boolean> =
        settingsDataSource.setBirthDate(date).subscribeOn(Schedulers.io())

    fun changeCurrentUserName(firstName: String, lastName: String): Single<Boolean> =
        settingsDataSource.changeName(firstName, lastName).subscribeOn(Schedulers.io())

    fun changeCurrentUserPass(pass: String): Single<Boolean> =
        settingsDataSource.changePassword(pass).subscribeOn(Schedulers.io())

    fun getCurrentUserBlocked(): Single<List<UserInfo>> =
        settingsDataSource.getBlocked().subscribeOn(Schedulers.io())

    fun unblockUser(userId: Long): Completable =
        settingsDataSource.unblockUser(userId).subscribeOn(Schedulers.io())

    fun getCurrentUserPrivacy(): Single<Map<Long, PrivacyType>> =
        settingsDataSource.getPrivacyData().subscribeOn(Schedulers.io()).map { data ->
            data.associate {
                it.id to it.status
            }
        }

    fun changePrivacy(friendId: Long, privacyType: PrivacyType): Completable =
        settingsDataSource.changePrivacyData(friendId, privacyType).subscribeOn(Schedulers.io())

    fun deleteCurrentAccount(password: String): Completable =
        settingsDataSource.deleteAccount(password).subscribeOn(Schedulers.io())


    companion object {
        private const val TAG = "Settings Repository"
    }
}