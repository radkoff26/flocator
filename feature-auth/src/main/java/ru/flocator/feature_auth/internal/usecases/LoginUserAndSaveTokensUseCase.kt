package ru.flocator.feature_auth.internal.usecases

import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import ru.flocator.data.models.auth.UserCredentialsDto
import ru.flocator.data.token.TokenPreferences
import ru.flocator.feature_auth.internal.repository.AuthorizationRepository
import javax.inject.Inject

internal class LoginUserAndSaveTokensUseCase @Inject constructor(
    private val authorizationRepository: AuthorizationRepository,
    private val tokenPreferences: TokenPreferences
) {

    operator fun invoke(userCredentialsDto: UserCredentialsDto): Completable {
        val compositeDisposable = CompositeDisposable()
        return Completable.create { emitter ->
            compositeDisposable.add(
                authorizationRepository.loginUser(userCredentialsDto)
                    .subscribeOn(Schedulers.io())
                    .subscribe(
                        {
                            tokenPreferences.setRefreshToken(it.refreshToken)
                            tokenPreferences.setAccessToken(it.accessToken)
                            emitter.onComplete()
                            compositeDisposable.dispose()
                        },
                        {
                            emitter.onError(it)
                            compositeDisposable.dispose()
                        }
                    )
            )
        }.subscribeOn(Schedulers.io())
    }
}