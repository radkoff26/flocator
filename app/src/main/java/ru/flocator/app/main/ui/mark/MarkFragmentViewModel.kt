package ru.flocator.app.main.ui.mark

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.flocator.app.common.cache.runtime.PhotoCacheLiveData
import ru.flocator.app.common.repository.MainRepository
import ru.flocator.app.common.storage.db.entities.MarkWithPhotos
import ru.flocator.app.main.MainSection
import ru.flocator.app.main.ui.mark.data.MarkFragmentState
import ru.flocator.app.main.models.dto.UsernameDto
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
class MarkFragmentViewModel @Inject constructor(
    private val repository: MainRepository,

    ) : ViewModel(), MainSection {
    private val _markLiveData = MutableLiveData<MarkWithPhotos?>(null)
    private val _userNameLiveData = MutableLiveData<UsernameDto?>(null)
    private val _fragmentStateLiveData: MutableLiveData<MarkFragmentState> = MutableLiveData(
        MarkFragmentState.Loading
    )

    val markLiveData: LiveData<MarkWithPhotos?> = _markLiveData
    val userNameLiveData: LiveData<UsernameDto?> = _userNameLiveData
    val photosStateLiveData: PhotoCacheLiveData = PhotoCacheLiveData(QUALITY_FACTOR)
    val markFragmentStateLiveData: LiveData<MarkFragmentState> = _fragmentStateLiveData

    private val compositeDisposable = CompositeDisposable()

    private var markId: Long? = null
    private var userId: Long? = null

    fun initialize(markId: Long, userId: Long) {
        this.markId = markId
        this.userId = userId
        loadData()
    }

    fun loadData() {
        if (_fragmentStateLiveData.value != MarkFragmentState.Loading) {
            _fragmentStateLiveData.value = MarkFragmentState.Loading
        }
        if (_markLiveData.value == null) {
            loadMark()
        } else {
            _fragmentStateLiveData.value = MarkFragmentState.Loaded
        }
    }

    private fun loadMark() {
        if (markId == null || userId == null) {
            return
        }
        compositeDisposable.add(
            repository.restApi.getMark(markId!!, userId!!)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        _markLiveData.value = it.toMarkWithPhotos()
                        it.photos.forEach { uri ->
                            photosStateLiveData.requestPhotoLoading(uri)
                        }
                        _fragmentStateLiveData.value = MarkFragmentState.Loaded
                        loadAuthorData()
                    },
                    {
                        Log.e(TAG, "loadMark: failed to load mark!", it)
                        _fragmentStateLiveData.value = MarkFragmentState.Failed(it)
                    }
                )
        )
    }

    private fun loadAuthorData() {
        _markLiveData.value!!
        compositeDisposable.add(
            repository.restApi.getUser(_markLiveData.value!!.mark.authorId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        _userNameLiveData.value = UsernameDto(
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
        if (markId == null || userId == null) {
            return
        }
        if (_markLiveData.value!!.mark.hasUserLiked) {
            unlikeMark()
            compositeDisposable.add(
                repository.restApi.unlikeMark(markId!!, userId!!)
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
                repository.restApi.likeMark(markId!!, userId!!)
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

    fun loadPhotoByUri(uri: String) {
        if (_markLiveData.value == null || photosStateLiveData.isLoaded(uri)) {
            return
        }
        photosStateLiveData.requestPhotoLoading(uri)
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

    companion object {
        const val TAG = "Mark Fragment"
        const val QUALITY_FACTOR = 25
    }
}