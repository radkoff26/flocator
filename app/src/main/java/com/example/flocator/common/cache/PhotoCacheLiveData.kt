package com.example.flocator.common.cache

import android.graphics.Bitmap
import android.util.Log
import android.util.LruCache
import androidx.lifecycle.*
import com.example.flocator.common.utils.LoadUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

class PhotoCacheLiveData(
    private val qualityFactor: Int,
    initialUris: Collection<String> = emptyList()
) : LiveData<LruCache<String, PhotoState>>() {
    private val compositeDisposable = CompositeDisposable()
    private val maxCacheSize = (Runtime.getRuntime().maxMemory() / 1024).toInt() / 2

    init {
        value = object : LruCache<String, PhotoState>(maxCacheSize) {
            override fun sizeOf(key: String?, value: PhotoState?): Int {
                return if (value is PhotoState.Loaded) value.bitmap.byteCount / 1024 else 0
            }
        }
        initialUris.forEach {
            requestPhotoLoading(it)
        }
    }

    fun requestPhotoLoading(uri: String) {
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

    // Dangerous function: can lead to NPE, so invoke only if made sure
    fun getPhoto(uri: String): Bitmap? {
        val photo = value!![uri]
        if (photo != null && photo is PhotoState.Loaded) {
            return photo.bitmap
        }
        return null
    }

    fun containsUri(uri: String) = getPhoto(uri) != null

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
