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
                val initialRequest = chain.request()
                chain.proceedWithToken(accessToken).doIfAuthFailed {
                    chain.tryRefreshingToken(initialRequest)
                }
            } else {
                chain.tryRefreshingToken()
            }
        } else chain.proceed(chain.request())
    }

    private fun Chain.tryRefreshingToken(initialRequest: Request? = null): Response {
        val firstRequest = initialRequest ?: request()
        val initialRefreshToken = tokenPreferences.getRefreshToken()
        // In case when data race is underway, one thread locks monitor and refreshes tokens
        // Another thread is waiting for monitor to release
        return synchronized(this@AuthInterceptor) {
            val refreshToken = tokenPreferences.getRefreshToken()
            // In data race case, when waiting took place, checking if tokens have already been updated
            if (refreshToken != initialRefreshToken) {
                val accessToken = tokenPreferences.getAccessToken()
                return if (accessToken != null) {
                    proceedWithToken(accessToken).throwErrorIfAuthFailed()
                } else {
                    throwError()
                }
            }
            if (refreshToken != null) {
                refreshToken(refreshToken, firstRequest)
            } else {
                throwError()
            }
        }
    }

    private fun Chain.refreshToken(refreshToken: String, firstRequest: Request): Response {
        val tokenPair = getNewTokenPair(refreshToken)
        return if (tokenPair != null) {
            tokenPreferences.updateTokens(tokenPair.refreshToken, tokenPair.accessToken)
            proceedWithToken(tokenPair.accessToken, firstRequest)
                .throwErrorIfAuthFailed()
        } else {
            throwError()
        }
    }

    private fun Response.throwErrorIfAuthFailed(): Response =
        doIfAuthFailed {
            throwError()
        }

    private fun Response.doIfAuthFailed(block: () -> Unit): Response {
        if (code() == 403) {
            block.invoke()
        }
        return this
    }

    private fun Chain.getNewTokenPair(refreshToken: String): TokenPair? {
        val tokenRequest = createRefreshTokenRequest(refreshToken)
        val tokenPairResponse = proceed(tokenRequest)
        if (tokenPairResponse.code() != 200) {
            return null
        }
        return gson.fromJson(tokenPairResponse.body()!!.string(), TokenPair::class.java)
    }


    private fun Chain.createRefreshTokenRequest(token: String) =
        request().newBuilder()
            .url(Constants.BASE_URL + ApiPaths.AUTH_REFRESH + "?token=$token").get().build()

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