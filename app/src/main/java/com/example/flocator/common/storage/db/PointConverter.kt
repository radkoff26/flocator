package com.example.flocator.common.storage.db

import androidx.room.TypeConverter
import com.yandex.mapkit.geometry.Point

class PointConverter {
    companion object {
        private const val STRING_TO_POINT_SEPARATOR = ", "
    }

    @TypeConverter
    fun convertStringToPoint(string: String): Point {
        val strings = string.split(STRING_TO_POINT_SEPARATOR)
        val latitude = strings[0].toDouble()
        val longitude = strings[1].toDouble()
        return Point(
            latitude,
            longitude
        )
    }

    @TypeConverter
    fun convertPointToString(point: Point): String {
        return "${point.latitude}$STRING_TO_POINT_SEPARATOR${point.longitude}"
    }
}