package ru.flocator.core_map.internal.domain.holder

import com.google.android.gms.maps.model.LatLng
import io.reactivex.disposables.Disposable
import ru.flocator.core_map.api.entity.User
import ru.flocator.core_map.internal.domain.bitmap_creator.BitmapCreator
import ru.flocator.core_map.internal.domain.bitmap_creator.LocatedBitmapCreatorHolder
import ru.flocator.core_map.internal.domain.map_item.DisposableMapItem
import ru.flocator.core_map.internal.ui.views.UserView

internal data class UserHolder(
    val userViewHolder: LocatedUserViewHolder,
    val user: User,
    val markerId: String,
    var avatarRequestDisposable: Disposable? = null
) : DisposableMapItem {

    override fun getBitmapCreator(): BitmapCreator = userViewHolder.userView

    override fun getDisposables(): List<Disposable?> = listOf(avatarRequestDisposable)

    override fun getItemMarkerId(): String = markerId

    override fun getLocation(): LatLng = user.location
}

internal data class LocatedUserViewHolder(
    val userView: UserView,
    val location: LatLng
) : LocatedBitmapCreatorHolder {
    override fun getHolderLocation(): LatLng = location

    override fun getHolderBitmapCreator(): BitmapCreator = userView
}
