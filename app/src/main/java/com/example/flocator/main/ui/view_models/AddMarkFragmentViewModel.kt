package com.example.flocator.main.ui.view_models

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.flocator.main.data.AddMarkFragmentData
import com.example.flocator.main.data.CarouselItemState

class AddMarkFragmentViewModel: ViewModel() {
    private val _liveData = MutableLiveData(AddMarkFragmentData(emptyList()))
    val liveData: LiveData<AddMarkFragmentData> = _liveData

    fun updateLiveData(list: List<Uri>) {
        if (_liveData.value == null || _liveData.value!!.stateList.isEmpty()) {
            _liveData.value = AddMarkFragmentData(list.map { CarouselItemState(it, false) })
            return
        }
        val stateList = _liveData.value!!.stateList.toMutableList()
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
        _liveData.value = AddMarkFragmentData(stateList)
    }

    fun toggleItem(uri: Uri, newState: Boolean) {
        if (_liveData.value == null) {
            return
        }
        val list = _liveData.value!!.stateList.toMutableList()
        val index = list.indexOfFirst { it.uri == uri }
        list[index].isSelected = newState
        _liveData.value = AddMarkFragmentData(list)
    }

    fun removeItems() {
        if (_liveData.value == null || _liveData.value!!.stateList.isEmpty()) {
            return
        }
        val list = _liveData.value!!.stateList.toMutableList()
        var i = 0
        while (i < list.size) {
            if (list[i].isSelected) {
                list.removeAt(i)
            } else {
                i++
            }
        }
        Log.d("TAG", "LIST SIZE:" + list.size)
        _liveData.value = AddMarkFragmentData(list)
    }

    fun isAnyTaken(): Boolean {
        if (_liveData.value == null || _liveData.value!!.stateList.isEmpty()) {
            return false
        }
        return _liveData.value!!.stateList.any { it.isSelected }
    }
}