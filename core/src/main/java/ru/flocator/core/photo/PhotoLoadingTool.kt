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
            // TODO: remove hardcoded url
            val mUrl =
                URL("${Constants.BASE_URL}api/photo/compressed?uri=$uri&compressionFactor=$compressionFactor")
            val inputStream = mUrl.openStream()
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            if (bitmap == null) {
                it.onError(Exception("Image is not loaded!"))
                return@create
            }
            it.onSuccess(bitmap)
        }.subscribeOn(Schedulers.io())
    }
}