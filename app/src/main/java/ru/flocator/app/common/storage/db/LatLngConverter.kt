package ru.flocator.app.common.storage.db

import androidx.room.TypeConverter
import com.google.android.gms.maps.model.LatLng

class LatLngConverter {
    companion object {
        private const val STRING_TO_POINT_SEPARATOR = ", "
    }

    @TypeConverter
    fun convertStringToLatLng(string: String): LatLng {
        val strings = string.split(STRING_TO_POINT_SEPARATOR)
        val latitude = strings[0].toDouble()
        val longitude = strings[1].toDouble()
        return LatLng(
            latitude,
            longitude
        )
    }

    @TypeConverter
    fun convertLatLngToString(latLng: LatLng): String {
        return "${latLng.latitude}$STRING_TO_POINT_SEPARATOR${latLng.longitude}"
    }
}