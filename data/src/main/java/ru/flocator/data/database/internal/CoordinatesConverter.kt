package ru.flocator.data.database.internal

import androidx.room.TypeConverter
import ru.flocator.data.models.location.Coordinates

internal class CoordinatesConverter {
    companion object {
        private const val STRING_TO_POINT_SEPARATOR = ", "
    }

    @TypeConverter
    fun convertStringToLatLng(string: String): Coordinates {
        val strings = string.split(STRING_TO_POINT_SEPARATOR)
        val latitude = strings[0].toDouble()
        val longitude = strings[1].toDouble()
        return Coordinates(
            latitude,
            longitude
        )
    }

    @TypeConverter
    fun convertLatLngToString(latLng: Coordinates): String {
        return "${latLng.latitude}$STRING_TO_POINT_SEPARATOR${latLng.longitude}"
    }
}