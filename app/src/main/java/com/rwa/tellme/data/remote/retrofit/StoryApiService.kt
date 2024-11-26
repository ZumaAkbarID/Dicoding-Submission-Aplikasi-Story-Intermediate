package com.rwa.tellme.data.remote.retrofit

import com.rwa.tellme.data.remote.response.StoryResponse
import com.rwa.tellme.data.remote.response.StoryUploadResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface StoryApiService {
    @GET("stories")
    suspend fun getStories(): Response<StoryResponse>

    @GET("stories")
    suspend fun getStories(
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20
    ): StoryResponse

    @Multipart
    @POST("stories")
    suspend fun uploadStory(
        @Part("description") description: RequestBody,
        @Part photo: MultipartBody.Part,
        @Part("lat") lat: RequestBody?,
        @Part("lon") lon: RequestBody?
    ): Response<StoryUploadResponse>

    @GET("stories")
    suspend fun getStoriesWithLocation(
        @Query("location") location : Int = 1,
    ): Response<StoryResponse>
}