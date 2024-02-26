package ru.flocator.app.data_source

import io.reactivex.Completable
import retrofit2.http.GET
import ru.flocator.data.api.ApiPaths

interface HandshakeDataSource {

    @GET(ApiPaths.USER_HANDSHAKE)
    fun testHandshake(): Completable
}