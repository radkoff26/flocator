package ru.flocator.map.internal.data.holder

import com.google.android.gms.maps.model.LatLng
import io.reactivex.disposables.Disposable
import ru.flocator.data.database.entities.User
import ru.flocator.map.internal.data.bitmap_creator.BitmapCreator
import ru.flocator.map.internal.data.bitmap_creator.LocatedBitmapCreatorHolder
import ru.flocator.map.internal.data.map_item.DisposableMapItem
import ru.flocator.map.internal.extensions.toLatLng
import ru.flocator.map.internal.ui.views.UserView

internal data class UserHolder(
    val userViewHolder: LocatedUserViewHolder,
    var user: User,
    val markerId: String,
    var avatarRequestDisposable: Disposable? = null
) : DisposableMapItem {

    override fun getBitmapCreator(): BitmapCreator = userViewHolder.userView

    override fun getDisposables(): List<Disposable?> = listOf(avatarRequestDisposable)

    override fun getItemMarkerId(): String = markerId

    override fun getLocation(): LatLng = user.location.toLatLng()
}

internal data class LocatedUserViewHolder(
    val userView: UserView,
    val location: LatLng
) : LocatedBitmapCreatorHolder {
    override fun getHolderLocation(): LatLng = location

    override fun getHolderBitmapCreator(): BitmapCreator = userView
}
