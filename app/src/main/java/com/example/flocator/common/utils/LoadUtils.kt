package com.example.flocator.common.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.flocator.common.config.Constants
import io.reactivex.Single
import io.reactivex.exceptions.UndeliverableException
import io.reactivex.schedulers.Schedulers
import java.io.ByteArrayOutputStream
import java.net.URL

class LoadUtils {
    companion object {
        fun loadPictureFromUrl(uri: String, qualityFactor: Int?): Single<Bitmap> {
            return Single.create {
                val mUrl = if (uri.contains("http")) { // TODO: eliminate this
                    URL(uri)
                } else {
                    URL("${Constants.BASE_URL}photo?uri=$uri")
                }
                val inputStream = mUrl.openStream()
                var bitmap = BitmapFactory.decodeStream(inputStream)
                if (bitmap == null) {
                    it.onError(Exception("Image is not loaded!"))
                    return@create
                }
                if (qualityFactor != null) {
                    val outputStream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.PNG, qualityFactor, outputStream)
                    val byteArray = outputStream.toByteArray()
                    bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                }
                inputStream.close()
                it.onSuccess(bitmap)
            }
                .subscribeOn(Schedulers.io())
                .retry { _, throwable ->
                    throwable !is UndeliverableException
                }
        }
    }
}