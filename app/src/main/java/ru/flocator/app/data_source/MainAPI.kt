package ru.flocator.app.data_source

import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.POST
import ru.flocator.core_dto.auth.UserCredentialsDto

interface MainAPI {

    @POST("user/login")
    fun loginUser(@Body userCredentialsDto: UserCredentialsDto): Single<Long>
}