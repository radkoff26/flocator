package com.example.flocator.main.ui.marks_list

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.flocator.common.cache.runtime.PhotoCacheLiveData
import com.example.flocator.common.repository.MainRepository
import com.example.flocator.main.data.Photo
import com.example.flocator.main.models.dto.UsernameDto
import com.example.flocator.main.ui.marks_list.data.ListMarkDto
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
class MarksListFragmentViewModel @Inject constructor(
    private val mainRepository: MainRepository
) : ViewModel() {
    private val _marksListLiveData: MutableLiveData<List<ListMarkDto>?> = MutableLiveData(null)
    private val _photoToMarkRelation: MutableMap<String, Long> = HashMap()
    private val photoCacheLiveData: PhotoCacheLiveData = PhotoCacheLiveData()

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

    private fun formatUsername(usernameDto: UsernameDto): String {
        return "${usernameDto.firstName} ${usernameDto.lastName}"
    }

    companion object {
        const val TAG = "Marks List Fragment View Model"
    }
}