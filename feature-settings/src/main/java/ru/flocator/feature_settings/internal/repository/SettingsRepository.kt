package ru.flocator.feature_settings.internal.repository

import android.util.Log
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import okhttp3.MultipartBody
import ru.flocator.core_api.api.AppRepository
import ru.flocator.core_connection.ConnectionWrapper
import ru.flocator.core_connection.live_data.ConnectionLiveData
import ru.flocator.core_data_store.user.info.UserInfo
import ru.flocator.core_database.entities.User
import ru.flocator.feature_settings.internal.data_source.SettingsAPI
import java.sql.Timestamp
import java.util.stream.Collectors
import javax.inject.Inject

internal class SettingsRepository @Inject constructor(
    private val settingsAPI: SettingsAPI,
    private val appRepository: AppRepository,
    private val connectionLiveData: ConnectionLiveData
) {
    private fun getAllFriendsOfUser(userId: Long): Single<List<User>> {
        val compositeDisposable = CompositeDisposable()
        return Single.create { emitter ->
            compositeDisposable.add(
                ConnectionWrapper.of(
                    settingsAPI.getUserFriendsLocated(userId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io()),
                    connectionLiveData
                )
                    .connect()
                    .subscribe(
                        {
                            emitter.onSuccess(it)
                            compositeDisposable.add(
                                appRepository.cacheDatabase.updateFriends(it)
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

    fun changeCurrentUserAva(ava: MultipartBody.Part): Single<Boolean> {
        return appRepository.userCredentialsCache.getUserCredentials().flatMap {
            settingsAPI.changeAvatar(
                it.userId,
                ava
            )
                .subscribeOn(Schedulers.io())
        }
            .subscribeOn(Schedulers.io())
    }

    fun changeCurrentUserBirthdate(date: Timestamp): Single<Boolean> {
        return appRepository.userCredentialsCache.getUserCredentials().flatMap {
            settingsAPI.setBirthDate(
                it.userId,
                date
            )
                .subscribeOn(Schedulers.io())
        }
            .subscribeOn(Schedulers.io())
    }

    fun changeCurrentUserName(firstName: String, lastName: String): Single<Boolean> {
        return appRepository.userCredentialsCache.getUserCredentials().flatMap {
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
        return appRepository.userCredentialsCache.getUserCredentials().flatMap {
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
        return appRepository.userCredentialsCache.getUserCredentials().flatMap {
            settingsAPI.getBlocked(
                it.userId
            )
                .observeOn(Schedulers.io())
        }
            .observeOn(Schedulers.io())
    }

    fun blockUser(userId: Long): Completable {
        return appRepository.userCredentialsCache.getUserCredentials().flatMapCompletable {
            settingsAPI.blockUser(
                it.userId,
                userId
            )
                .observeOn(Schedulers.io())
        }
            .observeOn(Schedulers.io())
    }

    fun unblockUser(userId: Long): Completable {
        return appRepository.userCredentialsCache.getUserCredentials().flatMapCompletable {
            settingsAPI.unblockUser(
                it.userId,
                userId
            )
                .observeOn(Schedulers.io())
        }
            .observeOn(Schedulers.io())
    }

    fun getCurrentUserPrivacy(): Single<Map<Long, String>> {
        return appRepository.userCredentialsCache.getUserCredentials().flatMap {
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
        return appRepository.userCredentialsCache.getUserCredentials().flatMapCompletable {
            settingsAPI.changePrivacyData(it.userId, friendId, status)
                .observeOn(Schedulers.io())
        }
            .observeOn(Schedulers.io())
    }

    fun deleteCurrentAccount(pass: String): Completable {
        return appRepository.userCredentialsCache.getUserCredentials().flatMapCompletable {
            settingsAPI.deleteAccount(it.userId, pass)
                .observeOn(Schedulers.io())
        }
            .observeOn(Schedulers.io())
    }

    fun getFriendsOfCurrentUser(): Single<List<User>> {
        return appRepository.userCredentialsCache.getUserCredentials().flatMap {
            getAllFriendsOfUser(it.userId)
                .observeOn(Schedulers.io())
        }
            .observeOn(Schedulers.io())
    }

    fun getCurrentUserInfo(): Single<UserInfo> {
        return appRepository.userCredentialsCache.getUserCredentials()
            .flatMap {
                getUser(it.userId)
                    .subscribeOn(Schedulers.io())
            }
            .subscribeOn(Schedulers.io())
    }

    fun getUser(userId: Long): Single<UserInfo> {
        return settingsAPI.getUser(userId).subscribeOn(Schedulers.io())
    }


    companion object {
        private const val TAG = "Settings Repository"
    }
}