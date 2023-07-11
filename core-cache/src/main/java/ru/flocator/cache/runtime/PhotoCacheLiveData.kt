package ru.flocator.cache.runtime

import android.graphics.Bitmap
import android.util.Log
import android.util.LruCache
import androidx.lifecycle.LiveData
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import ru.flocator.core_utils.LoadUtils

class PhotoCacheLiveData(
    private val qualityFactor: Int = 100
) : LiveData<LruCache<String, PhotoState>>() {
    private val compositeDisposable = CompositeDisposable()
    private val maxCacheSize = (Runtime.getRuntime().maxMemory() / 1024).toInt() / 2

    init {
        value = object : LruCache<String, PhotoState>(maxCacheSize) {
            override fun sizeOf(key: String?, value: PhotoState?): Int {
                return if (value is PhotoState.Loaded) value.bitmap.byteCount / 1024 else 0
            }
        }
    }

    fun requestPhotoLoading(uri: String) {
        if (value!![uri] == null || value!![uri] is PhotoState.Failed) {
            compositeDisposable.add(
                LoadUtils.loadPictureFromUrl(uri, qualityFactor)
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe {
                        value!!.put(uri, PhotoState.Loading)
                    }
                    .subscribe(
                        {
                            updateMap(uri, PhotoState.Loaded(it))
                        },
                        {
                            updateMap(uri, PhotoState.Failed(it))
                            Log.e(TAG, "requestPhotoLoading: failed to load photo!", it)
                        }
                    )
            )
        }
    }

    fun getPhotoAsync(uri: String): Single<Bitmap> {
        val photoState = value!![uri]
        if (photoState !is PhotoState.Loaded) {
            return LoadUtils.loadPictureFromUrl(uri, qualityFactor)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe {
                    value!!.put(uri, PhotoState.Loading)
                }
                .doOnSuccess {
                    updateMap(uri, PhotoState.Loaded(it))
                }
                .doOnError {
                    updateMap(uri, PhotoState.Failed(it))
                }
        }
        return Single.just(photoState.bitmap)
    }

    private fun getPhoto(uri: String): Bitmap? {
        val photo = value!![uri]
        if (photo != null && photo is PhotoState.Loaded) {
            return photo.bitmap
        }
        return null
    }

    fun isLoaded(uri: String) = getPhoto(uri) != null

    override fun onInactive() {
        super.onInactive()
        compositeDisposable.dispose()
    }

    private fun updateMap(key: String, photo: PhotoState) {
        value!!.put(key, photo)
        value = value // Updates state
    }

    companion object {
        const val TAG = "Photo Cache Manager"
    }
}
