package ru.flocator.map.internal.domain.holder

import com.google.android.gms.maps.model.LatLng
import io.reactivex.disposables.Disposable
import ru.flocator.map.api.entity.MapMark
import ru.flocator.map.internal.domain.bitmap_creator.BitmapCreator
import ru.flocator.map.internal.domain.bitmap_creator.LocatedBitmapCreatorHolder
import ru.flocator.map.internal.domain.map_item.DisposableMapItem
import ru.flocator.map.internal.extensions.toLatLng
import ru.flocator.map.internal.ui.views.MarkView

internal data class MarkHolder(
    val markViewHolder: LocatedMarkViewHolder,
    val mark: MapMark,
    val markerId: String,
    var thumbnailRequestDisposable: Disposable? = null,
    var avatarRequestDisposable: Disposable? = null
) : DisposableMapItem {
    override fun getItemMarkerId(): String = markerId

    override fun getBitmapCreator(): BitmapCreator = markViewHolder.markView

    override fun getLocation(): LatLng = mark.location.toLatLng()

    override fun getDisposables(): List<Disposable?> =
        listOf(thumbnailRequestDisposable, avatarRequestDisposable)
}

internal data class LocatedMarkViewHolder(
    val markView: MarkView,
    val location: LatLng
) : LocatedBitmapCreatorHolder {
    override fun getHolderLocation(): LatLng = location

    override fun getHolderBitmapCreator(): BitmapCreator = markView
}
