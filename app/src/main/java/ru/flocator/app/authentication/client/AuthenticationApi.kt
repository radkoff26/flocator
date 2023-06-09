package ru.flocator.app.authentication.client

import ru.flocator.app.authentication.client.dto.UserCredentialsDto
import ru.flocator.app.authentication.client.dto.UserRegistrationDto
import ru.flocator.app.common.storage.db.entities.User
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface AuthenticationApi {
    @POST("user/register")
    fun registerUser(@Body userRegistrationDto: UserRegistrationDto): Single<Boolean>

    @POST("user/login")
    fun loginUser(@Body userCredentialsDto: UserCredentialsDto): Single<Long>

    @GET("user/{userId}")
    fun getUserById(@Path("userId") userId: Long): Single<User>

    @GET("user/is_login_available")
    fun isLoginAvailable(@Query("login") login: String): Single<Boolean>

    @GET("user/is_email_available")
    fun isEmailAvailable(@Query("email") email: String): Single<Boolean>
}
