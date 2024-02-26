package ru.flocator.map.internal.extensions

import com.google.android.gms.maps.model.LatLng
import ru.flocator.data.models.location.Coordinates

internal fun LatLng.toCoordinates(): Coordinates = Coordinates(latitude, longitude)