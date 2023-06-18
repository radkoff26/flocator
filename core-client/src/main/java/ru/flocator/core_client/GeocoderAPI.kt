package ru.flocator.core_client

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query
import ru.flocator.core_config.Constants.GEOCODER_API_KEY
import ru.flocator.core_dto.address.AddressResponse

interface GeocoderAPI {
    @GET("1.x")
    fun getAddress(
        @Query("geocode") geoCode: String,
        @Query("apikey") apiKey: String = GEOCODER_API_KEY,
        @Query("format") format: String = "json",
        @Query("sco") scope: String = "latlong",
    ): Single<AddressResponse>
}