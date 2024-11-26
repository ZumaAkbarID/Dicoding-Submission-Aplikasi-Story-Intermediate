package com.rwa.tellme.data.remote.retrofit

import com.rwa.tellme.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object StoryApiConfig {
    private lateinit var dynamicAuthInterceptor: DynamicAuthInterceptor

    fun initAuthInterceptor(tokenProvider: () -> String?) {
        dynamicAuthInterceptor = DynamicAuthInterceptor(tokenProvider)
    }

    fun getProtectedService(): StoryApiService {
        val loggingInterceptor = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
        } else {
            HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.NONE)
        }
        val client: OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(dynamicAuthInterceptor)
            .build()
        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
        return retrofit.create(StoryApiService::class.java)
    }
}