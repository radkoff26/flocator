package ru.flocator.app

import android.content.Context
import android.content.Intent
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class StatusCodeInterceptor @Inject constructor(private val context: Context): Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        return chain.proceed(chain.request()).handleErrorIfOccurred()
    }

    private fun Response.handleErrorIfOccurred(): Response {
        when (code()) {
            408 -> context.sendBroadcast(Intent(Broadcasts.CONNECTION_FAILED))
            401 -> context.sendBroadcast(Intent(Broadcasts.AUTHORIZATION_FAILED))
        }
        return this
    }
}