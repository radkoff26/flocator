package ru.flocator.core_map.internal.domain.holder

import com.google.android.gms.maps.model.LatLng
import ru.flocator.core_map.internal.domain.map_item.MapItem
import ru.flocator.core_map.internal.domain.bitmap_creator.BitmapCreator
import ru.flocator.core_map.internal.domain.bitmap_creator.LocatedBitmapCreatorHolder
import ru.flocator.core_map.internal.domain.entity.MarkGroup
import ru.flocator.core_map.internal.ui.views.MarkGroupView

internal data class MarkGroupHolder(
    val markGroupViewHolder: LocatedMarkGroupViewHolder,
    val markGroup: MarkGroup,
    val markerId: String
) : MapItem {
    override fun getItemMarkerId(): String = markerId

    override fun getBitmapCreator(): BitmapCreator = markGroupViewHolder.markGroupView

    override fun getLocation(): LatLng = markGroup.center
}

internal data class LocatedMarkGroupViewHolder(
    val markGroupView: MarkGroupView,
    val location: LatLng
) : LocatedBitmapCreatorHolder {
    override fun getHolderLocation(): LatLng = location

    override fun getHolderBitmapCreator(): BitmapCreator = markGroupView
}
