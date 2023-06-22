package ru.flocator.feature_main.internal.marks_list.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import ru.flocator.cache.runtime.PhotoCacheLiveData
import ru.flocator.core_api.api.MainRepository
import ru.flocator.feature_main.internal.main.domain.photo.Photo
import ru.flocator.feature_main.internal.marks_list.domain.dto.ListMarkDto
import javax.inject.Inject

internal class MarksListFragmentViewModel @Inject constructor(
    private val mainRepository: MainRepository
) : ViewModel() {
    private val _marksListLiveData: MutableLiveData<List<ListMarkDto>?> = MutableLiveData(null)
    private val _photoToMarkRelation: MutableMap<String, Long> = HashMap()
    private val photoCacheLiveData: PhotoCacheLiveData =
        PhotoCacheLiveData()

    private val compositeDisposable = CompositeDisposable()

    val marksListLiveData: LiveData<List<ListMarkDto>?> = _marksListLiveData

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }

    fun requestUsernameLoading(userId: Long) {
        compositeDisposable.add(
            mainRepository.restApi.getUsername(userId)
                .observeOn(AndroidSchedulers.mainThread())
                .map(this::formatUsername)
                .subscribe(
                    {
                        updateUsername(userId, it)
                    },
                    {
                        Log.e(TAG, "requestUsernameLoading: failed to load username!", it)
                    }
                )
        )
    }

    fun requestPhotoLoading(uri: String) {
        if (!photoCacheLiveData.isLoaded(uri)) {
            compositeDisposable.add(
                photoCacheLiveData.getPhotoAsync(uri)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        {
                            updatePhoto(uri)
                        },
                        {
                            Log.e(TAG, "requestPhotoLoading: failed to load photo!", it)
                        }
                    )
            )
        }
    }

    fun submitMarks(marks: List<ListMarkDto>) {
        _marksListLiveData.value = ArrayList(marks)
        marks.forEach {
            _photoToMarkRelation[it.photo.uri] = it.mark.markId
        }
    }

    fun getUserId(): Single<Long> {
        return mainRepository.userInfoCache.getUserInfo()
            .observeOn(AndroidSchedulers.mainThread())
            .map(ru.flocator.core_data_store.user.info.UserInfo::userId)
    }

    private fun updatePhoto(uri: String) {
        val markId = _photoToMarkRelation[uri] ?: return
        val marks = _marksListLiveData.value ?: return
        val marksMutable = marks.toMutableList()
        val indexOfMark = marks.indexOfFirst {
            it.mark.markId == markId
        }
        marksMutable[indexOfMark] = marksMutable[indexOfMark].copy(
            photo = Photo(
                uri,
                photoCacheLiveData.value!![uri]
            )
        )
        _marksListLiveData.value = marksMutable
    }

    private fun updateUsername(authorId: Long, username: String) {
        val marksMutable = _marksListLiveData.value!!.toMutableList()
        var hasChanged = false
        for (i in marksMutable.indices) {
            if (marksMutable[i].mark.authorId == authorId) {
                hasChanged = true
                marksMutable[i] = marksMutable[i].copy(authorName = username)
            }
        }
        if (hasChanged) {
            _marksListLiveData.value = marksMutable
        }
    }

    private fun formatUsername(usernameDto: ru.flocator.core_dto.user_name.UsernameDto): String {
        return "${usernameDto.firstName} ${usernameDto.lastName}"
    }

    companion object {
        const val TAG = "Marks List Fragment View Model"
    }
}