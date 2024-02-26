package ru.flocator.feature_auth.internal.data.data_source

import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.http.*
import ru.flocator.data.api.ApiPaths
import ru.flocator.data.models.auth.UserRegistrationDto
import ru.flocator.feature_auth.internal.data.model.TokenPairDto

internal interface AuthDataSource {

    @POST(ApiPaths.AUTH_REGISTER)
    fun registerUser(@Body userRegistrationDto: UserRegistrationDto): Completable

    @GET(ApiPaths.AUTH_LOGIN)
    fun loginUser(
        @Query("username") login: String,
        @Query("password") password: String
    ): Single<String>

    @GET(ApiPaths.AUTH_REFRESH)
    fun refreshTokens(@Query("token") token: String): Single<TokenPairDto>

    @GET(ApiPaths.AUTH_IS_LOGIN_AVAILABLE)
    fun isLoginAvailable(@Query("login") login: String): Single<Boolean>

    @GET(ApiPaths.AUTH_IS_EMAIL_AVAILABLE)
    fun isEmailAvailable(@Query("email") email: String): Single<Boolean>
}
