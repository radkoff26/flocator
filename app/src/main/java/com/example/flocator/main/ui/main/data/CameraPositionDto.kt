package com.example.flocator.main.ui.main.data

import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition

data class CameraPositionDto(
    val target: Point,
    val zoom: Float,
    val azimuth: Float,
    val tilt: Float
) : java.io.Serializable {
    companion object {
        fun fromCameraPosition(cameraPosition: CameraPosition): CameraPositionDto =
            CameraPositionDto(
                cameraPosition.target,
                cameraPosition.zoom,
                cameraPosition.azimuth,
                cameraPosition.tilt
            )
    }

    fun toCameraPosition(): CameraPosition = CameraPosition(target, zoom, azimuth, tilt)
}
