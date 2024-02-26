package ru.flocator.feature_settings.internal.data.repository

import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import okhttp3.MultipartBody
import ru.flocator.feature_settings.internal.data.data_source.SettingsDataSource
import javax.inject.Inject

internal class PhotoRepository @Inject constructor(
    private val settingsDataSource: SettingsDataSource
) {

    fun postPhoto(photo: MultipartBody.Part): Single<String?> =
        settingsDataSource.postPhoto(listOf(photo)).subscribeOn(Schedulers.io()).map {
            it.firstOrNull()
        }
}