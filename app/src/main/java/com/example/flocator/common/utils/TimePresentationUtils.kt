package com.example.flocator.common.utils

import java.sql.Timestamp
import java.util.Calendar
import java.util.GregorianCalendar

object TimePresentationUtils {
    fun timestampToHumanPresentation(time: Timestamp): String {
        val timeNow = timeToTimeObject(System.currentTimeMillis())
        val timeThat = timeToTimeObject(time.time)
        if (timeNow.year != timeThat.year) {
            val delta = timeNow.year - timeThat.year
            return "$delta лет назад"
        }
        if (timeNow.month != timeThat.month) {
            val delta = timeNow.month - timeThat.month
            return "$delta месяцев назад"
        }
        if (timeNow.day != timeThat.day) {
            val delta = timeNow.day - timeThat.day
            return "$delta дней назад"
        }
        if (timeNow.hour != timeThat.hour) {
            val delta = timeNow.hour - timeThat.hour
            return "$delta часов назад"
        }
        if (timeNow.minute != timeThat.minute) {
            val delta = timeNow.minute - timeThat.minute
            return "$delta минут назад"
        }
        if (timeNow.second != timeThat.second) {
            val delta = timeNow.second - timeThat.second
            return "$delta секунд назад"
        }
        return ""
    }

    private fun timeToTimeObject(time: Long): TimeObject {
        val calendar = GregorianCalendar()
        calendar.timeInMillis = time
        return TimeObject(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH),
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            calendar.get(Calendar.SECOND)
        )
    }

    private data class TimeObject(
        val year: Int,
        val month: Int,
        val day: Int,
        val hour: Int,
        val minute: Int,
        val second: Int
    )
}