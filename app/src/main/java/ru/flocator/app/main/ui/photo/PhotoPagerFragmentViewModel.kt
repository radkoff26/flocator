package ru.flocator.app.main.ui.photo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.flocator.app.common.cache.runtime.PhotoCacheLiveData
import io.reactivex.disposables.CompositeDisposable

class PhotoPagerFragmentViewModel: ViewModel() {
    private val _toolbarDisplayedStateLiveData = MutableLiveData(true)

    val toolbarDisplayedStateLiveData: LiveData<Boolean> = _toolbarDisplayedStateLiveData
    val photoCacheLiveData: PhotoCacheLiveData = PhotoCacheLiveData(COMPRESSION_QUALITY)

    private val compositeDisposable = CompositeDisposable()

    fun requestPhotoLoading(uri: String) {
        photoCacheLiveData.requestPhotoLoading(uri)
    }

    fun switchToolbarState() {
        _toolbarDisplayedStateLiveData.value = !_toolbarDisplayedStateLiveData.value!!
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }

    companion object {
        const val TAG = "Photo Pager Fragment"
        const val COMPRESSION_QUALITY = 100
    }
}