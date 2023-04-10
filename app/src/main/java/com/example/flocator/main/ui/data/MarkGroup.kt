package com.example.flocator.main.ui.data

import com.example.flocator.main.models.Mark
import com.yandex.mapkit.geometry.Point
import java.util.Objects

data class MarkGroup(val marks: List<Mark>, val center: Point) {
    override fun equals(other: Any?): Boolean {
        if (other == null || other !is MarkGroup) {
            return false
        }
        return other.marks.toTypedArray().contentEquals(marks.toTypedArray())
                && other.center.longitude == center.longitude
                && other.center.latitude == center.latitude
    }

    override fun hashCode(): Int {
        val hash = marks.hashCode()
        return hash + Objects.hash(center) * 31
    }
}
