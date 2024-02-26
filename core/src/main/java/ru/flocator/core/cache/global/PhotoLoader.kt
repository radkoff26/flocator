package ru.flocator.core.cache.global

import android.graphics.Bitmap
import android.util.Log
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import ru.flocator.core.photo.PhotoLoadingTool

private typealias LoadingImage = Pair<String, Int>

class PhotoLoader(private val photoCacheManager: PhotoCacheManager) {
    private val state: MutableSet<LoadingImage> = HashSet()

    fun getPhoto(uri: String, qualityFactor: Int = 100): Single<Bitmap> {
        if (photoCacheManager.isPhotoCached(uri, qualityFactor)) {
            return Single.just(photoCacheManager.getPhotoFromCache(uri, qualityFactor)!!)
                .subscribeOn(Schedulers.io())
        }
        val processState = LoadingImage(uri, qualityFactor)
        state.add(processState)
        return PhotoLoadingTool.loadPictureFromUrl(
            uri,
            qualityFactor
        )
            .doOnSuccess {
                try {
                    photoCacheManager.savePhotoToCache(uri, it, qualityFactor)
                } catch (e: Exception) {
                    // Ignore any exceptions to prevent from onError callback
                    Log.e(TAG, "getPhoto: error while saving to cache!", e)
                } finally {
                    state.remove(processState)
                }
            }
            .doOnError {
                state.remove(processState)
            }
            .subscribeOn(Schedulers.io())
    }

    companion object {
        private const val TAG = "PhotoLoader_TAG"
    }
}