package ru.flocator.core_map.internal.domain.dto

import com.google.android.gms.maps.model.LatLng
import ru.flocator.core_database.entities.MarkWithPhotos
import java.util.*

internal data class MarkGroup(val marks: List<MarkWithPhotos>, val center: LatLng)
