package ru.flocator.feature_main.internal.data.repository

import android.net.Uri
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import ru.flocator.feature_main.internal.data.data_source.MainDataSource
import javax.inject.Inject

internal class PhotoRepository @Inject constructor(
    private val mainDataSource: MainDataSource
) {

    fun postPhotos(photos: Set<Map.Entry<Uri, ByteArray>>): Single<List<String>> {
        val photosToPost: List<MultipartBody.Part> = photos.map {
            val requestBody = RequestBody.create(MediaType.parse("image/*"), it.value)
            MultipartBody.Part.createFormData("photos", it.key.toString(), requestBody)
        }
        return mainDataSource.postPhotos(photosToPost).subscribeOn(Schedulers.io())
    }
}