package ru.flocator.core_database

import androidx.room.TypeConverter
import java.sql.Timestamp

class TimestampConverter {

    @TypeConverter
    fun convertLongToTimestamp(millis: Long): Timestamp {
        return Timestamp(millis)
    }

    @TypeConverter
    fun convertPointToString(timestamp: Timestamp): Long {
        return timestamp.time
    }
}