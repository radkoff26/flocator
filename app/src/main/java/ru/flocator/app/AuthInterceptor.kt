package ru.flocator.app

import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Interceptor.Chain
import okhttp3.Request
import okhttp3.Response
import ru.flocator.app.data_source.AccessRefreshDataSource
import ru.flocator.data.token.TokenPreferences
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val tokenPreferences: TokenPreferences,
    private val accessRefreshDataSource: AccessRefreshDataSource
) : Interceptor {

    override fun intercept(chain: Chain): Response {
        val url = chain.request().url()
        return if (needsAuthorization(url)) {
            val accessToken = tokenPreferences.getAccessToken()
            if (accessToken != null) {
                chain.proceedWithToken(accessToken)
            } else {
                val refreshToken = tokenPreferences.getRefreshToken()
                if (refreshToken != null) {
                    val tokenPairResponse =
                        accessRefreshDataSource.refreshTokens(refreshToken).execute()
                    if (tokenPairResponse.code() == 200) {
                        // Definitely know that body is present
                        val tokenPair = tokenPairResponse.body()!!
                        tokenPreferences.setAccessToken(tokenPair.accessToken)
                        tokenPreferences.setRefreshToken(tokenPair.refreshToken)
                        chain.proceedWithToken(tokenPair.accessToken)
                    } else {
                        authorizationErrorResponse()
                    }
                } else {
                    authorizationErrorResponse()
                }
            }
        } else chain.proceed(chain.request())
    }

    private fun authorizationErrorResponse() = Response.Builder().code(401).build()

    private fun needsAuthorization(url: HttpUrl): Boolean {
        return true
    }

    private fun Chain.proceedWithToken(token: String): Response {
        val requestWithToken = request().withBearerToken(token)
        return proceed(requestWithToken)
    }

    private fun Request.withBearerToken(token: String) = newBuilder()
        .addHeader("Authorization", "Bearer $token")
        .build()
}