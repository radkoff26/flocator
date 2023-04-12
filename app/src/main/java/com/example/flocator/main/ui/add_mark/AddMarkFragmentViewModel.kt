package com.example.flocator.main.ui.add_mark

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.flocator.main.api.ClientAPI
import com.example.flocator.main.api.GeocoderAPI
import com.example.flocator.main.ui.add_mark.data.AddMarkFragmentState
import com.example.flocator.main.ui.add_mark.data.CarouselItemState
import com.example.flocator.main.ui.add_mark.data.MarkDto
import com.google.gson.Gson
import com.yandex.mapkit.geometry.Point
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

@HiltViewModel
class AddMarkFragmentViewModel @Inject constructor(private val clientAPI: ClientAPI, private val geocoderApi: GeocoderAPI) : ViewModel() {
    private val _carouselLiveData = MutableLiveData<List<CarouselItemState>>(emptyList())
    private val _fragmentStateLiveData: MutableLiveData<AddMarkFragmentState> = MutableLiveData(
        AddMarkFragmentState.Editing)
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

    private fun obtainAddress() {
        compositeDisposable.add(
            geocoderApi.getAddress(getGeoCodeFormatted(_userPoint))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        _addressLiveData.value = it.address
                    },
                    {
                        Log.e(TAG, "obtainAddress: ${it.stackTraceToString()}")
                    }
                )
        )
    }

    private fun getGeoCodeFormatted(point: Point) = "${point.latitude}, ${point.longitude}"

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

    fun saveMark(mark: MarkDto, parts: List<MultipartBody.Part>) {
        val requestBodyMark = RequestBody.create(
            MediaType.parse("application/json"),
            Gson().toJson(mark)
        )
        compositeDisposable.add(
            clientAPI.postMark(requestBodyMark, parts)
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
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }

    companion object {
        const val TAG = "Add Mark Fragment View Model"
    }
}