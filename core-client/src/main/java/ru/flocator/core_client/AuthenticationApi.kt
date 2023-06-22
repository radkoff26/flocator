package ru.flocator.core_client

import io.reactivex.Single
import retrofit2.http.*
import ru.flocator.core_dto.auth.UserCredentialsDto
import ru.flocator.core_dto.auth.UserRegistrationDto

interface AuthenticationApi {
    @POST("user/register")
    fun registerUser(@Body userRegistrationDto: UserRegistrationDto): Single<Boolean>

    @POST("user/login")
    fun loginUser(@Body userCredentialsDto: UserCredentialsDto): Single<Long>

    @GET("user/{userId}")
    fun getUserById(@Path("userId") userId: Long): Single<ru.flocator.core_database.entities.User>

    @GET("user/is_login_available")
    fun isLoginAvailable(@Query("login") login: String): Single<Boolean>

    @GET("user/is_email_available")
    fun isEmailAvailable(@Query("email") email: String): Single<Boolean>
}
