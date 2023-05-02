package com.example.flocator.common.photo

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.*
import com.example.flocator.common.utils.LoadUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

class PhotoCacheLiveData(
    private val qualityFactor: Int,
    initialUris: Collection<String> = emptyList()
) : LiveData<MutableMap<String, PhotoState>>() {
    private val compositeDisposable = CompositeDisposable()

    init {
        value = HashMap(initialUris.size)
        initialUris.forEach {
            requestPhotoLoading(it)
        }
    }

    fun requestPhotoLoading(uri: String) {
        compositeDisposable.add(
            LoadUtils.loadPictureFromUrl(uri, qualityFactor)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe {
                    value!![uri] = PhotoState.Loading
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
        val map = value!!.toMutableMap()
        map[key] = photo
        value = map
    }

    companion object {
        const val TAG = "Photo Cache Manager"
    }
}
