package com.example.flocator.main.models

import com.yandex.mapkit.geometry.Point

class CameraStatus {
    private var _cameraStatusType: CameraStatusType = CameraStatusType.FIXED
    private var _markId: Long? = null
    val cameraStatusType
        get() = _cameraStatusType
    val markId
        get() = _markId
    var point: Point? = null

    fun setFollowOnFriendMark(markId: Long, point: Point) {
        this._cameraStatusType = CameraStatusType.FOLLOW_FRIEND
        this._markId = markId
        this.point = point
    }

    fun setFollowOnUserMark(userId: Long, point: Point) {
        this._cameraStatusType = CameraStatusType.FOLLOW_USER
        this._markId = userId
        this.point = point
    }

    fun setFixed() {
        this._cameraStatusType = CameraStatusType.FIXED
        this._markId = null
    }
}