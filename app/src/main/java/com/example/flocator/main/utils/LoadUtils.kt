package com.example.flocator.main.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.ByteArrayOutputStream
import java.net.URL

class LoadUtils {
    companion object {
        fun loadPictureFromUrl(url: String, qualityFactor: Int?): Single<Bitmap> {
            return Single.create<Bitmap> {
                val mUrl = URL(url)
                val inputStream = mUrl.openStream()
                var bitmap = BitmapFactory.decodeStream(inputStream)
                if (qualityFactor != null) {
                    val outputStream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.PNG, qualityFactor, outputStream)
                    val byteArray = outputStream.toByteArray()
                    bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                }
                inputStream.close()
                it.onSuccess(bitmap)
            }.subscribeOn(Schedulers.io())
        }
    }
}