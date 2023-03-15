package com.example.flocator.main.view_models

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.flocator.main.data.AddMarkFragmentData

class AddMarkFragmentViewModel: ViewModel() {
    val liveData = MutableLiveData(AddMarkFragmentData(emptyList()))

    fun updateLiveData(addMarkFragmentData: AddMarkFragmentData) {
        liveData.value = addMarkFragmentData
    }
}