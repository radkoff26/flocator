package ru.flocator.core_map.internal.domain.dto

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import ru.flocator.core_map.internal.domain.map_item.MapItem
import ru.flocator.core_map.internal.ui.BitmapCreator
import ru.flocator.core_map.internal.ui.views.MarkGroupView

internal data class MarkGroupViewDto(
    val markGroupView: MarkGroupView,
    val markGroup: MarkGroup,
    var marker: Marker? = null
) : MapItem {
    override fun getItemMarker(): Marker? = marker

    override fun getBitmapCreator(): BitmapCreator = markGroupView

    override fun getLocation(): LatLng = markGroup.center

    override fun setItemMarker(marker: Marker?) {
        this.marker = marker
    }
}
