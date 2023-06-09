package ru.flocator.app.main.ui.map.utils

import ru.flocator.app.common.storage.db.entities.User
import ru.flocator.app.common.utils.DistanceUtils
import ru.flocator.app.main.ui.map.domain.dto.MarkGroup
import com.google.android.gms.maps.model.LatLng

object MapComparingUtils {
    private val ZERO = LatLng(0.0, 0.0)

    object MarkGroupComparator: Comparator<MarkGroup> {
        override fun compare(o1: MarkGroup?, o2: MarkGroup?): Int {
            if (o1 == null && o2 == null) {
                return 0
            }
            if (o1 == null) {
                return -1
            }
            if (o2 == null) {
                return 1
            }
            val compareResult = o1.marks.size.compareTo(o2.marks.size)
            if (compareResult == 0) {
                val firstDistance = DistanceUtils.distanceBetweenToString(
                    o1.center,
                    ZERO
                )
                val secondDistance = DistanceUtils.distanceBetweenToString(o2.center,
                    ZERO
                )
                return firstDistance.compareTo(secondDistance)
            }
            return compareResult
        }
    }

    object UserComparator: Comparator<User> {
        override fun compare(o1: User?, o2: User?): Int {
            if (o1 == null && o2 == null) {
                return 0
            }
            if (o1 == null) {
                return -1
            }
            if (o2 == null) {
                return 1
            }
            return o1.id.compareTo(o2.id)
        }
    }
}