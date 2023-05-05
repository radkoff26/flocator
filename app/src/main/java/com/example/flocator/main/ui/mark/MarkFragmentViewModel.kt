package com.example.flocator.main.ui.mark

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.flocator.common.repository.MainRepository
import com.example.flocator.common.storage.db.entities.MarkWithPhotos
import com.example.flocator.common.utils.LoadUtils
import com.example.flocator.main.MainSection
import com.example.flocator.main.ui.mark.data.CarouselPhotoState
import com.example.flocator.main.ui.mark.data.MarkFragmentState
import com.example.flocator.main.ui.mark.data.UserNameDto
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class MarkFragmentViewModel constructor(
    private val repository: MainRepository,
    private val markId: Long,
    private val userId: Long
) : ViewModel(), MainSection {
    private val _markLiveData = MutableLiveData<MarkWithPhotos?>(null)
    private val _userNameLiveData = MutableLiveData<UserNameDto?>(null)
    private val _photosStateLiveData = MutableLiveData<List<CarouselPhotoState>?>(null)
    private val _fragmentStateLiveData: MutableLiveData<MarkFragmentState> = MutableLiveData(
        MarkFragmentState.Loading
    )

    private var photoLoadingState: MutableList<Boolean>? = null

    val markLiveData: LiveData<MarkWithPhotos?> = _markLiveData
    val userNameLiveData: LiveData<UserNameDto?> = _userNameLiveData
    val photosStateLiveData: LiveData<List<CarouselPhotoState>?> = _photosStateLiveData
    val markFragmentStateLiveData: LiveData<MarkFragmentState> = _fragmentStateLiveData

    private val compositeDisposable = CompositeDisposable()

    init {
        loadData()
    }

    fun loadData() {
        if (_fragmentStateLiveData.value != MarkFragmentState.Loading) {
            _fragmentStateLiveData.value = MarkFragmentState.Loading
        }
        loadMark()
    }

    private fun loadMark() {
        compositeDisposable.add(
            repository.restApi.getMark(markId, userId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        _markLiveData.value = it.toMarkWithPhotos()
                        _photosStateLiveData.value =
                            MutableList(it.photos.size) { CarouselPhotoState.Loading }
                        photoLoadingState = MutableList(it.photos.size) { false }
                        _fragmentStateLiveData.value = MarkFragmentState.Loaded
                        loadAuthorData()
                    },
                    {
                        _fragmentStateLiveData.value = MarkFragmentState.Failed(it)
                    }
                )
        )
    }

    private fun loadAuthorData() {
        _markLiveData.value!!
        compositeDisposable.add(
            repository.restApi.getUserInfo(_markLiveData.value!!.mark.authorId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        _userNameLiveData.value = UserNameDto(
                            it.firstName,
                            it.lastName
                        )
                    },
                    {
                        Log.e(TAG, "loadAuthorData: failed to load author data!", it)
                    }
                )
        )
    }

    fun toggleLike() {
        if (_markLiveData.value!!.mark.hasUserLiked) {
            unlikeMark()
            compositeDisposable.add(
                repository.restApi.unlikeMark(markId, userId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        {
                            loadMark()
                        },
                        {
                            Log.e(TAG, "toggleLike: error while liking photo", it)
                            likeMark()
                        }
                    )
            )
        } else {
            likeMark()
            compositeDisposable.add(
                repository.restApi.likeMark(markId, userId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        {
                            loadMark()
                        },
                        {
                            Log.e(TAG, "toggleLike: error while unliking photo", it)
                            unlikeMark()
                        }
                    )
            )
        }

    }

    fun loadPhotoByPosition(position: Int) {
        if (
            _markLiveData.value == null
            ||
            _photosStateLiveData.value == null
            ||
            photoLoadingState == null
            ||
            _photosStateLiveData.value!![position] is CarouselPhotoState.Loaded
        ) {
            return
        }
        if (!photoLoadingState!![position]) {
            photoLoadingState!![position] = true
            updateSinglePhotoState(CarouselPhotoState.Loading, position)
            compositeDisposable.add(
                LoadUtils.loadPictureFromUrl(
                    _markLiveData.value!!.photos[position].uri,
                    QUALITY_FACTOR
                )
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnDispose {
                        photoLoadingState!![position] = false
                    }
                    .subscribe(
                        {
                            updateSinglePhotoState(CarouselPhotoState.Loaded(it), position)
                        },
                        {
                            updateSinglePhotoState(CarouselPhotoState.Failed(it), position)
                        }
                    )
            )
        }

    }

    private fun likeMark() {
        val mark = _markLiveData.value!!
        mark.mark.likesCount++
        mark.mark.hasUserLiked = true
        _markLiveData.value = mark
    }

    private fun unlikeMark() {
        val mark = _markLiveData.value!!
        mark.mark.likesCount--
        mark.mark.hasUserLiked = false
        _markLiveData.value = mark
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }

    private fun updateSinglePhotoState(carouselPhotoState: CarouselPhotoState, position: Int) {
        val list = _photosStateLiveData.value!!.toMutableList()
        list[position] = carouselPhotoState
        _photosStateLiveData.value = list
    }

    companion object {
        const val TAG = "Mark Fragment"
        const val QUALITY_FACTOR = 40
    }
}