package ru.flocator.feature_main.internal.ui.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import ru.flocator.core.cache.runtime.PhotoCacheLiveData
import ru.flocator.core.section.MainSection
import ru.flocator.data.database.entities.MarkWithPhotos
import ru.flocator.feature_main.internal.data.model.fragment.MarkFragmentState
import ru.flocator.feature_main.internal.data.model.user_name.UsernameDto
import ru.flocator.feature_main.internal.data.repository.MarkRepository
import ru.flocator.feature_main.internal.data.repository.UserRepository
import javax.inject.Inject

internal class MarkFragmentViewModel @Inject constructor(
    private val markRepository: MarkRepository,
    private val userRepository: UserRepository
) : ViewModel(), MainSection {
    private val _markLiveData = MutableLiveData<MarkWithPhotos?>(null)
    private val _userNameLiveData =
        MutableLiveData<UsernameDto?>(null)
    private val _fragmentStateLiveData: MutableLiveData<MarkFragmentState> = MutableLiveData(
        MarkFragmentState.Loading
    )

    val markLiveData: LiveData<MarkWithPhotos?> = _markLiveData
    val userNameLiveData: LiveData<UsernameDto?> = _userNameLiveData
    val photosStateLiveData: PhotoCacheLiveData = PhotoCacheLiveData(QUALITY_FACTOR)
    val markFragmentStateLiveData: LiveData<MarkFragmentState> = _fragmentStateLiveData

    private val compositeDisposable = CompositeDisposable()

    private var markId: Long? = null

    fun initialize(markId: Long) {
        this.markId = markId
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
        if (markId == null) {
            return
        }
        compositeDisposable.add(
            markRepository.getMark(markId!!)
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
            userRepository.getUsername(_markLiveData.value!!.mark.authorId)
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
        if (markId == null) {
            return
        }
        if (_markLiveData.value!!.mark.hasUserLiked) {
            unlikeMark()
            compositeDisposable.add(
                markRepository.unlikeMark(markId!!)
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
                markRepository.likeMark(markId!!)
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