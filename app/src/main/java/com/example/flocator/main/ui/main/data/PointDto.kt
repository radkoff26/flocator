package com.example.flocator.main.ui.main.data

import android.os.Parcel
import android.os.Parcelable

data class PointDto(
    val latitude: Double,
    val longitude: Double
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readDouble(),
        parcel.readDouble()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeDouble(latitude)
        parcel.writeDouble(longitude)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PointDto> {
        override fun createFromParcel(parcel: Parcel): PointDto {
            return PointDto(parcel)
        }

        override fun newArray(size: Int): Array<PointDto?> {
            return arrayOfNulls(size)
        }
    }
}