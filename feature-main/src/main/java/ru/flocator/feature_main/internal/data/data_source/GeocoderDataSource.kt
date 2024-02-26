package ru.flocator.feature_main.internal.data.data_source

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query
import ru.flocator.core.config.Constants.GEOCODER_API_KEY
import ru.flocator.core.dependencies.Dependencies
import ru.flocator.data.models.address.AddressResponse

internal interface GeocoderDataSource : Dependencies {
    @GET("1.x")
    fun getAddress(
        @Query("geocode") geoCode: String,
        @Query("apikey") apiKey: String = GEOCODER_API_KEY,
        @Query("format") format: String = "json",
        @Query("sco") scope: String = "latlong",
    ): Single<AddressResponse>
}