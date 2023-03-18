package com.example.flocator.main.view_models

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.flocator.main.data.AddMarkFragmentData
import com.example.flocator.main.data.CarouselItemState

class AddMarkFragmentViewModel: ViewModel() {
    val liveData = MutableLiveData(AddMarkFragmentData(emptyList()))

    fun updateLiveData(list: List<Uri>) {
        val stateList = liveData.value!!.stateList.toMutableList()
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
        liveData.value = AddMarkFragmentData(stateList)
    }

    fun toggleItem(index: Int) {
        val list = liveData.value!!.stateList.toMutableList()
        list[index].isSelected = true
        liveData.value = AddMarkFragmentData(list)
    }

    fun removeItems() {
        val list = liveData.value!!.stateList.toMutableList()
        var i = 0
        while (i < list.size) {
            if (list[i].isSelected) {
                list.removeAt(i)
            } else {
                i++
            }
        }
        liveData.value = AddMarkFragmentData(list)
    }
}