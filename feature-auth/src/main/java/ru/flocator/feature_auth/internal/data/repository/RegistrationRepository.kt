package ru.flocator.feature_auth.internal.data.repository

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import ru.flocator.data.models.auth.UserRegistrationDto
import ru.flocator.feature_auth.internal.data.data_source.AuthDataSource
import javax.inject.Inject

internal class RegistrationRepository @Inject constructor(
    private val authDataSource: AuthDataSource
) {

    fun registerUser(userRegistrationDto: UserRegistrationDto): Completable {
        return authDataSource.registerUser(userRegistrationDto).subscribeOn(Schedulers.io())
    }

    fun isLoginAvailable(login: String): Single<Boolean> {
        return authDataSource.isLoginAvailable(login).subscribeOn(Schedulers.io())
    }

    fun isEmailAvailable(email: String): Single<Boolean> {
        return authDataSource.isEmailAvailable(email).subscribeOn(Schedulers.io())
    }
}