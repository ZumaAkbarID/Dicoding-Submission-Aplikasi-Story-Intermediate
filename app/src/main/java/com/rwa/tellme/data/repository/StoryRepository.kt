package com.rwa.tellme.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.map
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import androidx.paging.map
import com.rwa.tellme.data.Result
import com.rwa.tellme.data.local.database.StoryDatabase
import com.rwa.tellme.data.model.StoryModel
import com.rwa.tellme.data.remote.StoryRemoteMediator
import com.rwa.tellme.data.remote.response.StoryUploadResponse
import com.rwa.tellme.data.remote.retrofit.StoryApiService
import com.rwa.tellme.utils.AppExecutors
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class StoryRepository private constructor(
    private val storyApiService: StoryApiService,
    private val storyDatabase: StoryDatabase,
    private val appExecutors: AppExecutors
) {

    fun getAllStoryWithPager(): LiveData<PagingData<StoryModel>> {
        @OptIn(ExperimentalPagingApi::class)
        return Pager(
            config = PagingConfig(
                pageSize = 5
            ),
            remoteMediator = StoryRemoteMediator(storyDatabase, storyApiService),
            pagingSourceFactory = {
                storyDatabase.storyDao().getAllStory()
            }
        ).liveData.map { pagingData ->
            pagingData.map { storyEntity ->
                StoryModel(
                    id = storyEntity.id,
                    name = storyEntity.name,
                    photo = storyEntity.photo,
                    description = storyEntity.description,
                    createdAt = storyEntity.createdAt,
                    lon = storyEntity.lon,
                    lat = storyEntity.lat
                )
            }
        }
    }

    suspend fun getAllStory(): LiveData<Result<List<StoryModel>>> {
        val result = MediatorLiveData<Result<List<StoryModel>>>()
        result.value = Result.Loading

        try {
            val response = storyApiService.getStories()

            if (response.isSuccessful) {
                val storyResponse = response.body()
                val storyList = storyResponse?.listStory?.mapNotNull { listItem ->
                    listItem?.let {
                        StoryModel(
                            id = it.id ?: "",
                            name = it.name ?: "Unknown Name",
                            photo = it.photoUrl ?: "",
                            description = it.description ?: "No Description",
                            createdAt = it.createdAt ?: "Unknown Date",
                            lon = it.lon ?: 0.0,
                            lat = it.lat ?: 0.0
                        )
                    }
                } ?: emptyList()

                appExecutors.mainThread.execute {
                    result.value = Result.Success(storyList)
                }
            } else {
                appExecutors.mainThread.execute {
                    result.value = Result.Error("Failed fetch data: ${response.message()}")
                }
            }
        } catch (e: Exception) {
            appExecutors.mainThread.execute {
                result.value = Result.Error("Failed fetch data: ${e.message}")
            }
        }

        return result
    }

    suspend fun uploadStory(
        description: String,
        photoFile: File,
        lat: Double? = null,
        lon: Double? = null
    ): Result<StoryUploadResponse> {
        return try {
            val descriptionBody = description.toRequestBody("text/plain".toMediaType())

            val photoRequestBody = photoFile.asRequestBody("image/jpeg".toMediaType())
            val photoPart = MultipartBody.Part.createFormData(
                "photo",
                photoFile.name,
                photoRequestBody
            )

            val latBody = lat?.toString()?.toRequestBody("text/plain".toMediaType())
            val lonBody = lon?.toString()?.toRequestBody("text/plain".toMediaType())

            val response = storyApiService.uploadStory(
                descriptionBody,
                photoPart,
                latBody,
                lonBody
            )

            if (response.isSuccessful) {
                val responseBody = response.body()
                if (responseBody != null) {
                    Result.Success(responseBody)
                } else {
                    Result.Error("Response body is null")
                }
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Unknown error"
                Result.Error("API call failed: $errorMessage")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error occurred")
        }
    }

    suspend fun getAllStoryWithLocation(): LiveData<Result<List<StoryModel>>> {
        val result = MediatorLiveData<Result<List<StoryModel>>>()
        result.value = Result.Loading

        try {
            val response = storyApiService.getStoriesWithLocation()

            if (response.isSuccessful) {
                val storyResponse = response.body()
                val storyList = storyResponse?.listStory?.mapNotNull { listItem ->
                    listItem?.let {
                        StoryModel(
                            id = it.id ?: "",
                            name = it.name ?: "Unknown Name",
                            photo = it.photoUrl ?: "",
                            description = it.description ?: "No Description",
                            createdAt = it.createdAt ?: "Unknown Date",
                            lon = it.lon ?: 0.0,
                            lat = it.lat ?: 0.0
                        )
                    }
                } ?: emptyList()

                appExecutors.mainThread.execute {
                    result.value = Result.Success(storyList)
                }
            } else {
                appExecutors.mainThread.execute {
                    result.value = Result.Error("Failed fetch data: ${response.message()}")
                }
            }
        } catch (e: Exception) {
            appExecutors.mainThread.execute {
                result.value = Result.Error("Failed fetch data: ${e.message}")
            }
        }

        return result
    }

    companion object {
        @Volatile
        private var instance: StoryRepository? = null
        fun getInstance(
            storyApiService: StoryApiService,
            storyDatabase: StoryDatabase,
            appExecutors: AppExecutors
        ): StoryRepository =
            instance ?: synchronized(this) {
                instance ?: StoryRepository(storyApiService, storyDatabase, appExecutors)
            }.also { instance = it }
    }
}