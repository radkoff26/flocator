package ru.flocator.feature_auth.internal.repository

import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import ru.flocator.data.models.auth.UserCredentialsDto
import ru.flocator.feature_auth.internal.data.TokenPairDto
import ru.flocator.feature_auth.internal.data_source.AuthDataSource
import javax.inject.Inject

internal class AuthorizationRepository @Inject constructor(
    private val authDataSource: AuthDataSource
) {

    fun loginUser(userCredentialsDto: UserCredentialsDto): Single<TokenPairDto> {
        return authDataSource.loginUser(userCredentialsDto).subscribeOn(Schedulers.io()).flatMap {
            refreshTokens(it)
        }
    }

    private fun refreshTokens(token: String): Single<TokenPairDto> {
        return authDataSource.refreshTokens(token)
    }
}