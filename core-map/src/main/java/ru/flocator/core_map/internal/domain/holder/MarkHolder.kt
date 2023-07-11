package ru.flocator.core_map.internal.domain.holder

import com.google.android.gms.maps.model.LatLng
import io.reactivex.disposables.Disposable
import ru.flocator.core_map.api.entity.Mark
import ru.flocator.core_map.internal.domain.bitmap_creator.BitmapCreator
import ru.flocator.core_map.internal.domain.bitmap_creator.LocatedBitmapCreatorHolder
import ru.flocator.core_map.internal.domain.map_item.DisposableMapItem
import ru.flocator.core_map.internal.ui.views.MarkView

internal data class MarkHolder(
    val markViewHolder: LocatedMarkViewHolder,
    val mark: Mark,
    val markerId: String,
    var thumbnailRequestDisposable: Disposable? = null,
    var avatarRequestDisposable: Disposable? = null
) : DisposableMapItem {
    override fun getItemMarkerId(): String = markerId

    override fun getBitmapCreator(): BitmapCreator = markViewHolder.markView

    override fun getLocation(): LatLng = mark.location

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
