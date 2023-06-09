package ru.flocator.app.map.domain.dto

import ru.flocator.app.map.domain.map_item.MapItem
import ru.flocator.app.map.ui.BitmapCreator
import ru.flocator.app.map.ui.views.MarkGroupView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker

data class MarkGroupViewDto(
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
