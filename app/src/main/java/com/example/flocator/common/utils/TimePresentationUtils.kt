package com.example.flocator.common.utils

import java.sql.Timestamp

object TimePresentationUtils {
    fun timestampToHumanPresentation(time: Timestamp): String {
        val timeNow = timeToTimeObject(System.currentTimeMillis() - time.time)
        if (timeNow.years > 0) {
            val delta = timeNow.years
            return "$delta лет назад"
        }
        if (timeNow.months > 0) {
            val delta = timeNow.months
            return "$delta месяцев назад"
        }
        if (timeNow.days > 0) {
            val delta = timeNow.days
            return "$delta дней назад"
        }
        if (timeNow.hours > 0) {
            val delta = timeNow.hours
            return "$delta часов назад"
        }
        if (timeNow.minutes > 0) {
            val delta = timeNow.minutes
            return "$delta минут назад"
        }
        if (timeNow.seconds > 0) {
            val delta = timeNow.seconds
            return "$delta секунд назад"
        }
        return ""
    }

    private fun timeToTimeObject(time: Long): TimeObject {
        var temp = time / 1000
        val seconds = temp % 60
        temp /= 60
        val minutes = temp % 60
        temp /= 60
        val hours = temp % 24
        temp /= 24
        val days = temp % 30
        temp /= 30
        val months = temp % 12
        temp /= 12
        val years = temp
        return TimeObject(
            years,
            months,
            days,
            hours,
            minutes,
            seconds
        )
    }

    private data class TimeObject(
        val years: Long,
        val months: Long,
        val days: Long,
        val hours: Long,
        val minutes: Long,
        val seconds: Long
    )
}