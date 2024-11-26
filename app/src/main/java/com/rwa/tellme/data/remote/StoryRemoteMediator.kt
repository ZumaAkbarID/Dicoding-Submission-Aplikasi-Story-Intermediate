package com.rwa.tellme.data.remote

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.rwa.tellme.data.local.database.RemoteKeys
import com.rwa.tellme.data.local.database.StoryDatabase
import com.rwa.tellme.data.local.database.StoryEntity
import com.rwa.tellme.data.remote.retrofit.StoryApiService

@OptIn(ExperimentalPagingApi::class)
class StoryRemoteMediator(
    private val database: StoryDatabase,
    private val apiService: StoryApiService
) : RemoteMediator<Int, StoryEntity>() {

    private companion object {
        const val INITIAL_PAGE_INDEX = 1
    }

    override suspend fun initialize(): InitializeAction {
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, StoryEntity>
    ): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH ->{
                val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
                remoteKeys?.nextKey?.minus(1) ?: INITIAL_PAGE_INDEX
            }
            LoadType.PREPEND -> {
                val remoteKeys = getRemoteKeyForFirstItem(state)
                val prevKey = remoteKeys?.prevKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                prevKey
            }
            LoadType.APPEND -> {
                val remoteKeys = getRemoteKeyForLastItem(state)
                val nextKey = remoteKeys?.nextKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                nextKey
            }
        }

        try {
            val response = apiService.getStories(page, state.config.pageSize)
            val responseData = response.listStory ?: emptyList()

            val storyEntities = responseData.map { storyItem ->
                StoryEntity(
                    id = storyItem?.id ?: "",
                    name = storyItem?.name ?: "",
                    description = storyItem?.description ?: "",
                    photo = storyItem?.photoUrl ?: "",
                    createdAt = storyItem?.createdAt ?: "",
                    lat = storyItem?.lat ?: 0.0,
                    lon = storyItem?.lon ?: 0.0,
                )
            }

            val endOfPaginationReached = storyEntities.isEmpty()

            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    database.remoteKeysDao().deleteRemoteKeys()
                    database.storyDao().deleteAll()
                }

                if(responseData.isNotEmpty()) {
                    val prevKey = if (page == 1) null else page - 1
                    val nextKey = if (endOfPaginationReached) null else page + 1
                    val keys = responseData.map {
                        RemoteKeys(id = it?.id ?: "", prevKey = prevKey, nextKey = nextKey)
                    }
                    database.remoteKeysDao().insertAll(keys)
                    database.storyDao().insertStory(storyEntities)
                }
            }

            return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (exception: Exception) {
            return MediatorResult.Error(exception)
        }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, StoryEntity>): RemoteKeys? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()?.let { data ->
            database.remoteKeysDao().getRemoteKeysId(data.id)
        }
    }
    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, StoryEntity>): RemoteKeys? {
        return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()?.let { data ->
            database.remoteKeysDao().getRemoteKeysId(data.id)
        }
    }
    private suspend fun getRemoteKeyClosestToCurrentPosition(state: PagingState<Int, StoryEntity>): RemoteKeys? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id?.let { id ->
                database.remoteKeysDao().getRemoteKeysId(id)
            }
        }
    }
}