package com.example.flocator.main.ui.main.data

import com.example.flocator.common.storage.db.entities.MarkWithPhotos
import com.example.flocator.main.ui.main.views.BitmapCreator
import com.example.flocator.main.ui.main.views.map.DisposableMapItem
import com.example.flocator.main.ui.main.views.map.MapItem
import com.example.flocator.main.ui.main.views.mark.MarkView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import io.reactivex.disposables.Disposable

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
