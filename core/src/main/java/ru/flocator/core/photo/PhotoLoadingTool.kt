package ru.flocator.core.photo

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import ru.flocator.core.config.Constants
import java.io.ByteArrayOutputStream
import java.net.URL

object PhotoLoadingTool {

    fun loadPictureFromUrl(uri: String, qualityFactor: Int?): Single<Bitmap> {
        return Single.create {
            val factor = qualityFactor ?: 100
            val compressionFactor = factor.toFloat() / 100
            val mUrl =
                URL("${Constants.BASE_URL}photo/compressed?uri=$uri&compressionFactor=$compressionFactor")
            val inputStream = mUrl.openStream()
            var bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
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
            it.onSuccess(bitmap)
        }.subscribeOn(Schedulers.io())
    }
}