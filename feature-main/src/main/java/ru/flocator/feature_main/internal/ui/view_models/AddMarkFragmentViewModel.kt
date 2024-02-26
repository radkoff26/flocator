package ru.flocator.feature_main.internal.ui.view_models

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import ru.flocator.data.models.location.Coordinates
import ru.flocator.feature_main.internal.data.model.carousel.CarouselEditableItemState
import ru.flocator.feature_main.internal.data.model.fragment.AddMarkFragmentState
import ru.flocator.feature_main.internal.data.model.mark.AddMarkDto
import ru.flocator.feature_main.internal.data.repository.AddressRepository
import ru.flocator.feature_main.internal.domain.PostMarkWithPhotosUseCase
import javax.inject.Inject

internal class AddMarkFragmentViewModel @Inject constructor(
    private val addressRepository: AddressRepository,
    private val postMarkWithPhotosUseCase: PostMarkWithPhotosUseCase
) : ViewModel() {
    private val _carouselLiveData = MutableLiveData<List<CarouselEditableItemState>>(emptyList())
    private val _fragmentStateLiveData: MutableLiveData<AddMarkFragmentState> = MutableLiveData(
        AddMarkFragmentState.Editing
    )
    private val _addressLiveData = MutableLiveData<String>(null)
    private lateinit var _userPoint: Coordinates

    private val compositeDisposable = CompositeDisposable()

    val carouselLiveData: LiveData<List<CarouselEditableItemState>> = _carouselLiveData
    val fragmentStateLiveData: LiveData<AddMarkFragmentState> = _fragmentStateLiveData
    val addressLiveData: LiveData<String> = _addressLiveData
    val userPoint: Coordinates
        get() = _userPoint

    fun updateUserPoint(point: Coordinates) {
        _userPoint = point
        obtainAddress()
    }

    private fun changeFragmentState(addMarkFragmentState: AddMarkFragmentState) {
        _fragmentStateLiveData.value = addMarkFragmentState
    }

    private fun obtainAddress() {
        compositeDisposable.add(
            addressRepository.getAddress(_userPoint)
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

    fun updatePhotosLiveData(list: List<Uri>) {
        if (_carouselLiveData.value == null || _carouselLiveData.value!!.isEmpty()) {
            _carouselLiveData.value = list.map { CarouselEditableItemState(it, false) }
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
                stateList.add(CarouselEditableItemState(uri, false))
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
            postMarkWithPhotosUseCase(parts, mark)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe {
                    changeFragmentState(AddMarkFragmentState.Saving)
                }
                .subscribe(
                    {
                        changeFragmentState(AddMarkFragmentState.Saved)
                    },
                    {
                        Log.e(
                            TAG,
                            "Error while posting mark!",
                            it
                        )
                        changeFragmentState(AddMarkFragmentState.Failed(it))
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