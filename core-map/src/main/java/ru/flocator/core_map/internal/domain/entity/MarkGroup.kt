package ru.flocator.core_map.internal.domain.entity

import com.google.android.gms.maps.model.LatLng
import ru.flocator.core_map.api.entity.Mark

internal data class MarkGroup(val marks: List<Mark>, val center: LatLng) {

}
