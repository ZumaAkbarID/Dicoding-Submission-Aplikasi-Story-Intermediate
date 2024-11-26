package com.rwa.tellme.di

import android.content.Context
import com.rwa.tellme.data.local.database.StoryDatabase
import com.rwa.tellme.data.remote.retrofit.StoryApiConfig
import com.rwa.tellme.data.repository.StoryRepository
import com.rwa.tellme.utils.AppExecutors

object StoryInjection {
    fun provideStoryRepository(context: Context): StoryRepository {
        val apiService = StoryApiConfig.getProtectedService()
        val database = StoryDatabase.getDatabase(context)
        val appExecutors = AppExecutors()

        return StoryRepository.getInstance(apiService, database, appExecutors)
    }
}