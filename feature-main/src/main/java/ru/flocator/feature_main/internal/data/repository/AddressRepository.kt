package ru.flocator.feature_main.internal.data.repository

import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import ru.flocator.data.models.address.AddressResponse
import ru.flocator.data.models.location.Coordinates
import ru.flocator.feature_main.internal.data.data_source.GeocoderDataSource
import javax.inject.Inject

internal class AddressRepository @Inject constructor(
    private val geocoderDataSource: GeocoderDataSource
) {

    fun getAddress(coordinates: Coordinates): Single<String> {
        return geocoderDataSource.getAddress("${coordinates.latitude}, ${coordinates.longitude}")
            .map(AddressResponse::address)
            .subscribeOn(Schedulers.io())
    }
}