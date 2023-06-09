package ru.flocator.app.main.api

import ru.flocator.app.common.config.Constants.GEOCODER_API_KEY
import ru.flocator.app.main.data.AddressResponse
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface GeocoderAPI {
    @GET("1.x")
    fun getAddress(
        @Query("geocode") geoCode: String,
        @Query("apikey") apiKey: String = GEOCODER_API_KEY,
        @Query("format") format: String = "json",
        @Query("sco") scope: String = "latlong",
    ): Single<AddressResponse>
}