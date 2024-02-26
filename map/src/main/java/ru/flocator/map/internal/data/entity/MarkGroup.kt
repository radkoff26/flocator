package ru.flocator.map.internal.data.entity

import com.google.android.gms.maps.model.LatLng
import ru.flocator.map.api.entity.MapMark

internal data class MarkGroup(val marks: List<MapMark>, val center: LatLng)