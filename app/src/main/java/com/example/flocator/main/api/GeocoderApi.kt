package com.example.flocator.main.api

import com.example.flocator.common.config.Constants.GEOCODER_API_KEY
import com.example.flocator.main.ui.data.response.AddressResponse
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface GeocoderApi {
    @GET("1.x")
    fun getAddress(
        @Query("geocode") geoCode: String,
        @Query("apikey") apiKey: String = GEOCODER_API_KEY,
        @Query("format") format: String = "json",
        @Query("sco") scope: String = "latlong",
    ): Single<AddressResponse>
}