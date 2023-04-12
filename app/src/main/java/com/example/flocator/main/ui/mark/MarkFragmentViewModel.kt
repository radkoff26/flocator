package com.example.flocator.main.ui.mark

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.flocator.main.MainSection
import com.example.flocator.main.api.ClientAPI
import com.example.flocator.main.models.Mark
import com.example.flocator.main.ui.mark.data.MarkFragmentState
import com.example.flocator.main.ui.mark.data.UserNameDto
import com.example.flocator.main.utils.LoadUtils
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class MarkFragmentViewModel @AssistedInject constructor(
    private val clientAPI: ClientAPI,
    @Assisted("markId") val markId: Long,
    @Assisted("userId") val userId: Long
) : ViewModel(), MainSection {
    private val _markLiveData = MutableLiveData<Mark?>(null)
    private val _userNameLiveData = MutableLiveData<UserNameDto?>(null)
    private val _photosLiveData = MutableLiveData<List<Bitmap?>?>(null)
    private val _fragmentStateLiveData: MutableLiveData<MarkFragmentState> = MutableLiveData(
        MarkFragmentState.Loading
    )
    private var photoLoadingState: MutableList<Boolean?>? = null

    val markLiveData: LiveData<Mark?> = _markLiveData
    val userNameLiveData: LiveData<UserNameDto?> = _userNameLiveData
    val photosLiveData: LiveData<List<Bitmap?>?> = _photosLiveData
    val markFragmentStateLiveData: LiveData<MarkFragmentState> = _fragmentStateLiveData

    private val compositeDisposable = CompositeDisposable()

    init {
        compositeDisposable.add(
            clientAPI.getMark(markId, userId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        _markLiveData.value = it
                        _photosLiveData.value = MutableList(it.photos.size) { null }
                        photoLoadingState = MutableList(it.photos.size) { null }
                        _fragmentStateLiveData.value = MarkFragmentState.Loaded

                        compositeDisposable.add(
                            clientAPI.getUser(it.authorId)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(
                                    { userInfo ->
                                        _userNameLiveData.value =
                                            UserNameDto(userInfo.firstName, userInfo.lastName)
                                    },
                                    { throwable ->
                                        _fragmentStateLiveData.value =
                                            MarkFragmentState.Failed(throwable)
                                    }
                                )
                        )
                    },
                    {
                        _fragmentStateLiveData.value = MarkFragmentState.Failed(it)
                    }
                ),
        )
    }

    fun toggleLike() {
        if (_markLiveData.value!!.hasUserLiked) {
            unlikeMark()
            compositeDisposable.add(
                clientAPI.unlikeMark(markId, userId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        {
                            compositeDisposable.add(
                                clientAPI.getMark(markId, userId)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe { it ->
                                        val mark = _markLiveData.value!!
                                        mark.likesCount = it.likesCount
                                        mark.hasUserLiked = it.hasUserLiked
                                        _markLiveData.value = mark
                                    }
                            )
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
                clientAPI.likeMark(markId, userId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        {
                            compositeDisposable.add(
                                clientAPI.getMark(markId, userId)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe { it ->
                                        val mark = _markLiveData.value!!
                                        mark.likesCount = it.likesCount
                                        mark.hasUserLiked = it.hasUserLiked
                                        _markLiveData.value = mark
                                    }
                            )
                        },
                        {
                            Log.e(TAG, "toggleLike: error while unliking photo", it)
                            unlikeMark()
                        }
                    )
            )
        }

    }

    private fun likeMark() {
        val mark = _markLiveData.value!!
        mark.likesCount++
        mark.hasUserLiked = true
        _markLiveData.value = mark
    }

    private fun unlikeMark() {
        val mark = _markLiveData.value!!
        mark.likesCount--
        mark.hasUserLiked = false
        _markLiveData.value = mark
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }

    fun loadPhotoByPosition(position: Int) {
        if (_markLiveData.value == null || photoLoadingState == null) {
            return
        }
        if (photoLoadingState!![position] == null || photoLoadingState!![position] == false) {
            compositeDisposable.add(
                LoadUtils.loadPictureFromUrl(_markLiveData.value!!.photos[position], QUALITY_FACTOR)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        {
                            photoLoadingState!![position] = true
                            updatePhoto(it, position)
                        },
                        {
                            photoLoadingState!![position] = false
                        }
                    )
            )
        }
    }

    private fun updatePhoto(bitmap: Bitmap, position: Int) {
        val photos = _photosLiveData.value!!.toMutableList()
        photos[position] = bitmap
        _photosLiveData.value = photos
    }

    @AssistedFactory
    interface Factory {
        fun build(
            @Assisted("markId") markId: Long,
            @Assisted("userId") userId: Long
        ): MarkFragmentViewModel
    }

    companion object {
        const val TAG = "Mark Fragment"
        const val QUALITY_FACTOR = 40
    }
}