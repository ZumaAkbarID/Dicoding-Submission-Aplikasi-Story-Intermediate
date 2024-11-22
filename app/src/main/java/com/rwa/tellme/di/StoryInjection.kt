package com.rwa.tellme.di

import android.content.Context
import com.rwa.tellme.data.pref.UserPreference
import com.rwa.tellme.data.pref.dataStore
import com.rwa.tellme.data.remote.retrofit.ApiConfig
import com.rwa.tellme.data.repository.StoryRepository
import com.rwa.tellme.utils.AppExecutors

object StoryInjection {
    fun provideStoryRepository(context: Context): StoryRepository {
        val pref = UserPreference.getInstance(context.dataStore)
        val user = pref.getToken()
        val apiService = ApiConfig.getProtectedService(user!!)
        val appExecutors = AppExecutors()
        return StoryRepository.getInstance(apiService, appExecutors)
    }
}