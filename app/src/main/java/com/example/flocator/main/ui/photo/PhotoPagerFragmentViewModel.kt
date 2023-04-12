package com.example.flocator.main.ui.photo

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.flocator.main.utils.LoadUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class PhotoPagerFragmentViewModel constructor(private val uriList: List<String>) : ViewModel() {
    private val _photosLiveData: MutableLiveData<List<Bitmap?>> =
        MutableLiveData(MutableList(uriList.size) { null })
    val photosLiveData: LiveData<List<Bitmap?>> = _photosLiveData

    private val photosState: MutableList<Boolean?> = MutableList(uriList.size) { null }

    private val compositeDisposable = CompositeDisposable()

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }

    fun requestPhotoLoading(position: Int) {
        if (photosState[position] == null || photosState[position] == false) {
            compositeDisposable.add(
                LoadUtils.loadPictureFromUrl(uriList[position], COMPRESSION_QUALITY)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        {
                            updatePhoto(position, it)
                            photosState[position] = true
                        },
                        {
                            photosState[position] = false
                        }
                    )
            )
        }
    }

    private fun updatePhoto(position: Int, bitmap: Bitmap) {
        val photos = _photosLiveData.value!!.toMutableList()
        photos[position] = bitmap
        _photosLiveData.value = photos
    }

    companion object {
        const val TAG = "Photo Pager Fragment"
        const val COMPRESSION_QUALITY = 100
    }
}