package com.example.flocator.main.ui.main.data

import android.os.Parcel
import android.os.Parcelable
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition

data class CameraPositionDto(
    val target: PointDto,
    val zoom: Float,
    val azimuth: Float,
    val tilt: Float
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readParcelable(PointDto::class.java.classLoader)!!,
        parcel.readFloat(),
        parcel.readFloat(),
        parcel.readFloat()
    )

    fun toCameraPosition(): CameraPosition =
        CameraPosition(Point(target.latitude, target.longitude), zoom, azimuth, tilt)

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeFloat(zoom)
        parcel.writeFloat(azimuth)
        parcel.writeFloat(tilt)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CameraPositionDto> {
        override fun createFromParcel(parcel: Parcel): CameraPositionDto {
            return CameraPositionDto(parcel)
        }

        override fun newArray(size: Int): Array<CameraPositionDto?> {
            return arrayOfNulls(size)
        }

        fun fromCameraPosition(cameraPosition: CameraPosition): CameraPositionDto =
            CameraPositionDto(
                PointDto(
                    cameraPosition.target.latitude,
                    cameraPosition.target.longitude
                ),
                cameraPosition.zoom,
                cameraPosition.azimuth,
                cameraPosition.tilt
            )
    }
}
