package com.example.flocator.main.ui.map.domain.dto

import com.example.flocator.common.storage.db.entities.MarkWithPhotos
import com.example.flocator.main.ui.map.domain.map_item.DisposableMapItem
import com.example.flocator.main.ui.map.ui.BitmapCreator
import com.example.flocator.main.ui.map.ui.views.MarkView
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
