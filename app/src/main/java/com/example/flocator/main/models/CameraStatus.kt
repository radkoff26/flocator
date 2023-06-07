package com.example.flocator.main.models

import com.google.android.gms.maps.model.LatLng

class CameraStatus {
    private var _isCameraFixed: Boolean = true
    private var _userId: Long? = null
    val isCameraFixed: Boolean
        get() = _isCameraFixed
    val userId
        get() = _userId
    var latLng: LatLng? = null

    fun setFollowUser(userId: Long, latLng: LatLng) {
        this._isCameraFixed = false
        this._userId = userId
        this.latLng = latLng
    }

    fun setFixed() {
        this._isCameraFixed = true
        this._userId = null
    }
}