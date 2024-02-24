package ru.flocator.map.internal.utils

import com.google.android.gms.maps.model.LatLng
import ru.flocator.core.utils.DistanceUtils
import ru.flocator.data.database.entities.User
import ru.flocator.map.internal.domain.entity.MarkGroup

internal object MapComparingUtils {
    private val ZERO = LatLng(0.0, 0.0)

    object MarkGroupComparator : Comparator<MarkGroup> {
        override fun compare(o1: MarkGroup, o2: MarkGroup): Int {
            val compareResult = o1.marks.size.compareTo(o2.marks.size)
            if (compareResult == 0) {
                val firstDistance = DistanceUtils.calculateDistance(
                    o1.center,
                    ZERO
                )
                val secondDistance = DistanceUtils.calculateDistance(
                    o2.center,
                    ZERO
                )
                return firstDistance.compareTo(secondDistance)
            }
            return compareResult
        }
    }

    object UserComparator : Comparator<User> {
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
            return o1.userId.compareTo(o2.userId)
        }
    }
}