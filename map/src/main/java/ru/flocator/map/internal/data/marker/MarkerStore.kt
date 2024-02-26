package ru.flocator.map.internal.data.marker

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import ru.flocator.map.internal.data.bitmap_creator.LocatedBitmapCreatorHolder
import ru.flocator.map.internal.data.map_item.MapItem

internal typealias MarkerMap = MutableMap<String, Marker>

/**
 * Class which stores markers in an internal HashMap by marker id.
 * It's also responsible for drawing markers on the map provided by parent.
 * */
internal class MarkerStore : DefaultLifecycleObserver {
    private val markerMap: MarkerMap = HashMap()

    @Volatile
    private var isActive: Boolean = false

    @Volatile
    private var map: GoogleMap? = null

    override fun onResume(owner: LifecycleOwner) {
        isActive = true
    }

    override fun onPause(owner: LifecycleOwner) {
        isActive = false
    }

    override fun onDestroy(owner: LifecycleOwner) {
        markerMap.forEach { (_, marker) ->
            marker.remove()
        }
        markerMap.clear()
        map = null
    }

    fun setLifecycleOwner(lifecycleOwner: LifecycleOwner) {
        lifecycleOwner.lifecycle.addObserver(this)
    }

    fun setMap(map: GoogleMap) {
        this.map = map
    }

    /**
     * Method that creates a marker and draws this marker on the map.
     * @param [bitmapCreatorHolder] holder which provides [LocatedBitmapCreatorHolder] for creating marker.
     * @param [animationCallback] an optional callback which will be invoked after marker is created.
     * @return [String] - id of the created marker if one was created, otherwise - null.
     * */
    fun createMarker(
        bitmapCreatorHolder: LocatedBitmapCreatorHolder,
        animationCallback: ((marker: Marker) -> Unit)? = null
    ): String? {
        val bitmap = bitmapCreatorHolder.getHolderBitmapCreator().createBitmap()
        val bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmap)
        val marker = map?.addMarker(
            MarkerOptions()
                .position(bitmapCreatorHolder.getHolderLocation())
                .icon(bitmapDescriptor)
        )?.also {
            markerMap[it.id] = it
            animationCallback?.invoke(it)
        }
        return marker?.id
    }

    /**
     * Method that returns a marker by marker id.
     * @param markerId id of marker to receive.
     * @return instance of [Marker] if marker with such id exists, otherwise - null.
     * */
    fun getMarker(markerId: String): Marker? = markerMap[markerId]

    /**
     * Method that updates drawn marker in accordance with state of [MapItem] parameter.
     * @param mapItem instance of [MapItem] which provides data for marker update.
     * @return true if marker was found and updated, otherwise - false.
     * */
    fun updateMarker(mapItem: MapItem): Boolean {
        val marker = markerMap[mapItem.getItemMarkerId()] ?: return false
        val bitmap = mapItem.getBitmapCreator().createBitmap()
        val bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmap)
        marker.setIcon(bitmapDescriptor)
        marker.position = mapItem.getLocation()
        return true
    }

    /**
     * Method that removes particular marker from map.
     * @param markerId id of marker to remove.
     * */
    fun removeMarker(markerId: String) {
        val marker = markerMap[markerId] ?: return
        marker.remove()
        markerMap.remove(markerId)
    }

    fun isActive() = isActive && map != null
}