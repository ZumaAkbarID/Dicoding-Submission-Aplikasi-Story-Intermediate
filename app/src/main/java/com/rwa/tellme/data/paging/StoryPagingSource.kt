package com.rwa.tellme.data.paging

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.rwa.tellme.data.model.StoryModel
import com.rwa.tellme.data.remote.response.StoryResponse
import com.rwa.tellme.data.remote.retrofit.StoryApiService

class StoryPagingSource(private val storyApiService: StoryApiService) : PagingSource<Int, StoryModel>() {
    private companion object {
        const val INITIAL_PAGE_INDEX = 1
    }

    override fun getRefreshKey(state: PagingState<Int, StoryModel>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, StoryModel> {
        return try {
            val position = params.key ?: INITIAL_PAGE_INDEX
            val response = storyApiService.getStoriesWithLocation(location = 1)

            if (response.isSuccessful) {
                val body = response.body()
                val storyModels = body?.listStory?.mapNotNull { item ->
                    item?.let {
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

                LoadResult.Page(
                    data = storyModels,
                    prevKey = if (position == INITIAL_PAGE_INDEX) null else position - 1,
                    nextKey = if (storyModels.isEmpty()) null else position + 1
                )
            } else {
                LoadResult.Error(Exception("Error: ${response.message()}"))
            }
        } catch (exception: Exception) {
            LoadResult.Error(exception)
        }
    }
}