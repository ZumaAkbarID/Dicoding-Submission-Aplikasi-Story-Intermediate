package com.rwa.tellme.data.remote.retrofit

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response

class DynamicAuthInterceptor(private val tokenProvider: () -> String?) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenProvider()
        val request = chain.request().newBuilder()
            .apply {
                token?.let { addHeader("Authorization", "Bearer $it")
                    Log.d("BIJIX", "TOKEN: $it")
                }
            }
            .build()
        return chain.proceed(request)
    }
}
