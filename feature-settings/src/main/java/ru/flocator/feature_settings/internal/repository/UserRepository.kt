package ru.flocator.feature_settings.internal.repository

import android.util.Log
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import ru.flocator.data.database.ApplicationDatabase
import ru.flocator.data.database.entities.User
import ru.flocator.feature_settings.internal.data_source.SettingsDataSource
import javax.inject.Inject

internal class UserRepository @Inject constructor(
    private val settingsDataSource: SettingsDataSource,
    private val database: ApplicationDatabase
) {

    fun getFriendsOfCurrentUser(): Single<List<User>> =
        getAllFriendsOfUser().subscribeOn(Schedulers.io())

    private fun getAllFriendsOfUser(): Single<List<User>> {
        val compositeDisposable = CompositeDisposable()
        return Single.create { emitter ->
            compositeDisposable.add(
                settingsDataSource.getUserFriendsLocated()
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .subscribe(
                        {
                            emitter.onSuccess(it)
                            compositeDisposable.add(
                                database.userDao().updateTable(it)
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
        }.subscribeOn(Schedulers.io()).doOnDispose { compositeDisposable.dispose() }
    }

    companion object {
        const val TAG = "UserRepository_TAG"
    }
}