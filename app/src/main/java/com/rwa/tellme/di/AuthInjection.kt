package com.rwa.tellme.di

import android.content.Context
import com.rwa.tellme.data.repository.UserRepository
import com.rwa.tellme.data.pref.UserPreference
import com.rwa.tellme.data.pref.dataStore
import com.rwa.tellme.data.remote.retrofit.AuthApiConfig
import com.rwa.tellme.utils.AppExecutors

object AuthInjection {
    fun provideRepository(context: Context): UserRepository {
        val pref = UserPreference.getInstance(context.dataStore)
        val apiService = AuthApiConfig.getAuthService()
        val appExecutors = AppExecutors()
        return UserRepository.getInstance(pref, apiService, appExecutors)
    }
}