package ru.flocator.map.internal.extensions

import com.google.android.gms.maps.model.LatLng
import ru.flocator.data.models.location.Coordinates

internal fun Coordinates.toLatLng(): LatLng = LatLng(latitude, longitude)