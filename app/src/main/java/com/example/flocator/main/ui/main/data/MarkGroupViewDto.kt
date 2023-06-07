package com.example.flocator.main.ui.main.data

import com.example.flocator.main.ui.main.views.BitmapCreator
import com.example.flocator.main.ui.main.views.map.MapItem
import com.example.flocator.main.ui.main.views.mark_group.MarkGroupView
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
