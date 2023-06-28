package ru.flocator.feature_auth.internal.repository

import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import ru.flocator.core_dto.auth.UserCredentialsDto
import ru.flocator.core_dto.auth.UserRegistrationDto
import ru.flocator.feature_auth.internal.data_source.AuthenticationApi

internal class AuthRepository constructor(
    private val authenticationApi: AuthenticationApi
) {
    fun registerUser(userRegistrationDto: UserRegistrationDto): Single<Boolean> {
        return authenticationApi.registerUser(userRegistrationDto).subscribeOn(Schedulers.io())
    }

    fun isLoginAvailable(login: String): Single<Boolean> {
        return authenticationApi.isLoginAvailable(login).subscribeOn(Schedulers.io())
    }

    fun isEmailAvailable(email: String): Single<Boolean> {
        return authenticationApi.isEmailAvailable(email).subscribeOn(Schedulers.io())
    }

    fun loginUser(userCredentialsDto: UserCredentialsDto): Single<Long> {
        return authenticationApi.loginUser(userCredentialsDto).subscribeOn(Schedulers.io())
    }
}