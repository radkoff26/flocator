package ru.flocator.app

import android.content.Context
import android.content.Intent
import com.google.gson.Gson
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Interceptor.Chain
import okhttp3.Request
import okhttp3.Response
import ru.flocator.app.data.TokenPair
import ru.flocator.core.config.Constants
import ru.flocator.data.api.ApiPaths
import ru.flocator.data.token.TokenPreferences
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val context: Context,
    private val tokenPreferences: TokenPreferences,
    private val gson: Gson
) : Interceptor {

    override fun intercept(chain: Chain): Response {
        val url = chain.request().url()
        return if (needsAuthorization(url)) {
            val accessToken = tokenPreferences.getAccessToken()
            if (accessToken != null) {
                chain.proceedWithToken(accessToken).checkAuthSucceeded()
            } else {
                val currentRequest = chain.request()
                val refreshToken = tokenPreferences.getRefreshToken()
                if (refreshToken != null) {
                    val tokenPair = getNewTokenPair(chain, refreshToken)
                    if (tokenPair != null) {
                        tokenPreferences.setAccessToken(tokenPair.accessToken)
                        tokenPreferences.setRefreshToken(tokenPair.refreshToken)
                        chain.proceedWithToken(tokenPair.accessToken, currentRequest)
                            .checkAuthSucceeded()
                    } else {
                        throwError()
                    }
                } else {
                    throwError()
                }
            }
        } else chain.proceed(chain.request())
    }

    private fun Response.checkAuthSucceeded(): Response {
        if (code() == 403) {
            throwError()
        }
        return this
    }

    private fun getNewTokenPair(chain: Chain, refreshToken: String): TokenPair? {
        val tokenRequest = createRefreshTokenRequest(chain, refreshToken)
        val tokenPairResponse = chain.proceed(tokenRequest)
        if (tokenPairResponse.code() != 200) {
            return null
        }
        return gson.fromJson(tokenPairResponse.body()!!.string(), TokenPair::class.java)
    }


    private fun createRefreshTokenRequest(chain: Chain, token: String) =
        chain.request().newBuilder()
            .url(Constants.BASE_URL + ApiPaths.AUTH_REFRESH + "?refresh_token=$token").build()

    private fun throwError(): Nothing {
        context.sendBroadcast(Intent(Broadcasts.AUTHORIZATION_FAILED))
        throw Exception("Failed to perform request due to fail of authorization!")
    }

    private fun needsAuthorization(url: HttpUrl): Boolean {
        val trimmedUrl = url.toString()
            .replace(Constants.BASE_URL, "")
            .replaceAfter("?", "")
            .replace("?", "")
        return trimmedUrl !in NO_AUTH_URLS
    }

    private fun Chain.proceedWithToken(token: String, request: Request? = null): Response {
        val requestWithToken = (request ?: request()).withBearerToken(token)
        return proceed(requestWithToken)
    }

    private fun Request.withBearerToken(token: String) = newBuilder()
        .addHeader("Authorization", "Bearer $token")
        .build()

    companion object {
        private val NO_AUTH_URLS = listOf(
            ApiPaths.AUTH_REFRESH,
            ApiPaths.AUTH_LOGIN,
            ApiPaths.AUTH_IS_LOGIN_AVAILABLE,
            ApiPaths.AUTH_IS_EMAIL_AVAILABLE,
            ApiPaths.AUTH_REGISTER
        )
    }
}