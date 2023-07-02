package ru.flocator.core_map.internal.domain.dto

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import io.reactivex.disposables.Disposable
import ru.flocator.core_database.entities.User
import ru.flocator.core_map.internal.domain.map_item.DisposableMapItem
import ru.flocator.core_map.internal.ui.BitmapCreator
import ru.flocator.core_map.internal.ui.views.UserView

internal data class UserViewDto(
    val userView: UserView,
    val user: User,
    var marker: Marker? = null,
    var avatarRequestDisposable: Disposable? = null
): DisposableMapItem {

    override fun getBitmapCreator(): BitmapCreator = userView

    override fun getDisposables(): List<Disposable?> = listOf(avatarRequestDisposable)

    override fun getItemMarker(): Marker? = marker

    override fun setItemMarker(marker: Marker?) {
        this.marker = marker
    }

    override fun getLocation(): LatLng = user.location
}