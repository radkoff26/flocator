package com.example.flocator.main.ui.main.data

import com.example.flocator.common.storage.db.entities.MarkWithPhotos
import com.google.android.gms.maps.model.LatLng
import java.util.*

data class MarkGroup(val marks: List<MarkWithPhotos>, val center: LatLng) {
    override fun equals(other: Any?): Boolean {
        if (other == null || other !is MarkGroup) {
            return false
        }
        return other.marks == marks
                && other.center.longitude == center.longitude
                && other.center.latitude == center.latitude
    }

    override fun hashCode(): Int {
        val hash = marks.hashCode()
        return hash + Objects.hash(center) * 31
    }
}
