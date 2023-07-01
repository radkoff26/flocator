package ru.flocator.feature_auth.internal.repository

import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import ru.flocator.core_dto.auth.UserCredentialsDto
import ru.flocator.core_dto.auth.UserRegistrationDto
import ru.flocator.feature_auth.internal.data_source.AuthAPI
import javax.inject.Inject

internal class AuthRepository @Inject constructor(
    private val authApi: AuthAPI
) {
    fun registerUser(userRegistrationDto: UserRegistrationDto): Single<Boolean> {
        return authApi.registerUser(userRegistrationDto).subscribeOn(Schedulers.io())
    }

    fun isLoginAvailable(login: String): Single<Boolean> {
        return authApi.isLoginAvailable(login).subscribeOn(Schedulers.io())
    }

    fun isEmailAvailable(email: String): Single<Boolean> {
        return authApi.isEmailAvailable(email).subscribeOn(Schedulers.io())
    }

    fun loginUser(userCredentialsDto: UserCredentialsDto): Single<Long> {
        return authApi.loginUser(userCredentialsDto).subscribeOn(Schedulers.io())
    }
}