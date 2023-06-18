package ru.flocator.app.map.domain.dto

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import io.reactivex.disposables.Disposable
import ru.flocator.app.map.domain.map_item.DisposableMapItem
import ru.flocator.app.map.ui.BitmapCreator
import ru.flocator.app.map.ui.views.MarkView
import ru.flocator.core_database.entities.MarkWithPhotos

data class MarkViewDto(
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
