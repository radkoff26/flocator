package ru.flocator.data.database.internal

import androidx.room.TypeConverter
import java.sql.Timestamp

internal class TimestampConverter {

    @TypeConverter
    fun convertLongToTimestamp(millis: Long): Timestamp {
        return Timestamp(millis)
    }

    @TypeConverter
    fun convertPointToString(timestamp: Timestamp): Long {
        return timestamp.time
    }
}