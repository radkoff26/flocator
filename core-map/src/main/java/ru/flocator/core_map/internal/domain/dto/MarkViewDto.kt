package ru.flocator.core_map.internal.domain.dto

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import io.reactivex.disposables.Disposable
import ru.flocator.core_database.entities.MarkWithPhotos
import ru.flocator.core_map.internal.domain.map_item.DisposableMapItem
import ru.flocator.core_map.internal.ui.BitmapCreator
import ru.flocator.core_map.internal.ui.views.MarkView

internal data class MarkViewDto(
    val markView: MarkView,
    val mark: MarkWithPhotos,
    val userAvatarUri: String?,
    var marker: Marker? = null,
    var thumbnailRequestDisposable: Disposable? = null,
    var avatarRequestDisposable: Disposable? = null
) : DisposableMapItem {
    override fun getItemMarker(): Marker? = marker
    override fun getBitmapCreator(): BitmapCreator = markView
    override fun getLocation(): LatLng = mark.mark.location
    override fun setItemMarker(marker: Marker?) {
        this.marker = marker
    }
    override fun getDisposables(): List<Disposable?> =
        listOf(thumbnailRequestDisposable, avatarRequestDisposable)
}
