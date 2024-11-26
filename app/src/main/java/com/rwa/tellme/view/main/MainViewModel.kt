package com.rwa.tellme.view.main

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.rwa.tellme.data.model.UserModel
import com.rwa.tellme.data.pref.UserPreference
import com.rwa.tellme.data.remote.retrofit.StoryApiConfig
import com.rwa.tellme.data.repository.UserRepository
import kotlinx.coroutines.launch
import com.rwa.tellme.data.pref.dataStore
import kotlinx.coroutines.runBlocking

class MainViewModel(
    private val repository: UserRepository
) : ViewModel() {
    fun getSession(): LiveData<UserModel> {
        return repository.getSession().asLiveData()
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }

    fun clearTokenFromInterceptor() {
        StoryApiConfig.initAuthInterceptor { null }
    }

    fun initTokenInterceptor(context: Context) {
        StoryApiConfig.initAuthInterceptor {
            val pref = UserPreference.getInstance(context.dataStore)
            runBlocking { pref.getToken() }
        }
    }
}