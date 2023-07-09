package ru.flocator.core_utils

import android.content.res.Resources
import java.sql.Timestamp

object TimePresentationUtils {
    fun timestampToHumanPresentation(time: Timestamp, resources: Resources): String {
        val timeNow = timeToTimeObject(System.currentTimeMillis() - time.time)
        if (timeNow.years > 0) {
            val delta = timeNow.years
            return resources.getString(R.string.years, delta)
        }
        if (timeNow.months > 0) {
            val delta = timeNow.months
            return resources.getString(R.string.months, delta)
        }
        if (timeNow.days > 0) {
            val delta = timeNow.days
            return resources.getString(R.string.days, delta)
        }
        if (timeNow.hours > 0) {
            val delta = timeNow.hours
            return resources.getString(R.string.hours, delta)
        }
        if (timeNow.minutes > 0) {
            val delta = timeNow.minutes
            return resources.getString(R.string.minutes, delta)
        }
        if (timeNow.seconds > 0) {
            val delta = timeNow.seconds
            return resources.getString(R.string.seconds, delta)
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