package com.example.flocator.main.ui.map.domain.dto

import com.example.flocator.common.storage.db.entities.User
import com.example.flocator.main.ui.map.domain.map_item.DisposableMapItem
import com.example.flocator.main.ui.map.ui.BitmapCreator
import com.example.flocator.main.ui.map.ui.views.UserView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import io.reactivex.disposables.Disposable

data class UserViewDto(
    val userView: UserView,
    val user: User,
    var marker: Marker? = null,
    var avatarRequestDisposable: Disposable? = null
): DisposableMapItem {

    override fun getDisposables(): List<Disposable?> = listOf(avatarRequestDisposable)

    override fun getItemMarker(): Marker? = marker

    override fun getBitmapCreator(): BitmapCreator = userView

    override fun getLocation(): LatLng = user.location

    override fun setItemMarker(marker: Marker?) {
        this.marker = marker
    }
}