package com.example.flocator.main.ui.add_mark

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.flocator.common.repository.MainRepository
import com.example.flocator.common.storage.storage.user.UserData
import com.example.flocator.main.ui.add_mark.data.AddMarkDto
import com.example.flocator.main.ui.add_mark.data.AddMarkFragmentState
import com.example.flocator.main.ui.add_mark.data.CarouselItemState
import com.yandex.mapkit.geometry.Point
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
class AddMarkFragmentViewModel @Inject constructor(
    private val repository: MainRepository
) : ViewModel() {
    private val _carouselLiveData = MutableLiveData<List<CarouselItemState>>(emptyList())
    private val _fragmentStateLiveData: MutableLiveData<AddMarkFragmentState> = MutableLiveData(
        AddMarkFragmentState.Editing
    )
    private val _addressLiveData = MutableLiveData<String>(null)
    private lateinit var _userPoint: Point

    private val compositeDisposable = CompositeDisposable()

    val carouselLiveData: LiveData<List<CarouselItemState>> = _carouselLiveData
    val fragmentStateLiveData: LiveData<AddMarkFragmentState> = _fragmentStateLiveData
    val addressLiveData: LiveData<String> = _addressLiveData
    val userPoint: Point
        get() = _userPoint

    fun updateUserPoint(point: Point) {
        _userPoint = point
        obtainAddress()
    }

    private fun changeFragmentState(addMarkFragmentState: AddMarkFragmentState) {
        _fragmentStateLiveData.value = addMarkFragmentState
    }

    private fun getUserId(): Single<Long> {
        return repository.userCache.getUserData()
            .map(UserData::userId)
            .observeOn(AndroidSchedulers.mainThread())
    }

    private fun obtainAddress() {
        compositeDisposable.add(
            repository.restApi.getAddress(_userPoint)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        _addressLiveData.value = it
                    },
                    {
                        Log.e(TAG, "obtainAddress: ${it.stackTraceToString()}")
                    }
                )
        )
    }

    fun updateLiveData(list: List<Uri>) {
        if (_carouselLiveData.value == null || _carouselLiveData.value!!.isEmpty()) {
            _carouselLiveData.value = list.map { CarouselItemState(it, false) }
            return
        }
        val stateList = _carouselLiveData.value!!.toMutableList()
        val length = stateList.size - 1
        for (uri in list) {
            var isFound = false
            for (i in 0..length) {
                if (stateList[i].uri == uri) {
                    isFound = true
                    break
                }
            }
            if (!isFound) {
                stateList.add(CarouselItemState(uri, false))
            }
        }
        _carouselLiveData.value = stateList
    }

    fun toggleItem(uri: Uri, newState: Boolean) {
        if (_carouselLiveData.value == null) {
            return
        }
        val list = _carouselLiveData.value!!.toMutableList()
        val index = list.indexOfFirst { it.uri == uri }
        if (index != -1) {
            list[index].isSelected = newState
            _carouselLiveData.value = list
        }
    }

    fun removeItems() {
        if (_carouselLiveData.value == null || _carouselLiveData.value!!.isEmpty()) {
            return
        }
        val list = _carouselLiveData.value!!.toMutableList()
        var i = 0
        while (i < list.size) {
            if (list[i].isSelected) {
                list.removeAt(i)
            } else {
                i++
            }
        }
        _carouselLiveData.value = list
    }

    fun isAnyTaken(): Boolean {
        if (_carouselLiveData.value == null || _carouselLiveData.value!!.isEmpty()) {
            return false
        }
        return _carouselLiveData.value!!.any { it.isSelected }
    }

    fun saveMark(mark: AddMarkDto, parts: Set<Map.Entry<Uri, ByteArray>>) {
        compositeDisposable.add(
            getUserId().subscribe(
                {
                    mark.authorId = it
                    compositeDisposable.add(
                        repository.restApi.postMark(mark, parts)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .doOnSubscribe {
                                _fragmentStateLiveData.value = AddMarkFragmentState.Saving
                            }
                            .subscribe(
                                {
                                    Log.i(TAG, "Posted mark!")
                                    _fragmentStateLiveData.value = AddMarkFragmentState.Saved
                                },
                                {
                                    Log.e(
                                        TAG,
                                        "Error while posting mark!",
                                        it
                                    )
                                    _fragmentStateLiveData.value = AddMarkFragmentState.Failed(it)
                                }
                            )
                    )
                },
                {
                    changeFragmentState(
                        AddMarkFragmentState.Failed(
                            Throwable("Не авторизован!")
                        )
                    )
                    Log.e(TAG, "saveMark: error while getting user id!", it)
                }
            )
        )
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }

    companion object {
        const val TAG = "Add Mark Fragment View Model"
    }
}