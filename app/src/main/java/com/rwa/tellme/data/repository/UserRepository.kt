package com.rwa.tellme.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.google.gson.Gson
import com.rwa.tellme.data.model.UserModel
import com.rwa.tellme.data.pref.UserPreference
import com.rwa.tellme.data.remote.retrofit.AuthApiService
import com.rwa.tellme.utils.AppExecutors
import kotlinx.coroutines.flow.Flow
import com.rwa.tellme.data.Result
import com.rwa.tellme.data.remote.response.ErrorResponse

class UserRepository private constructor(
    private val userPreference: UserPreference,
    private val authApiService: AuthApiService,
    private val appExecutors: AppExecutors
) {

    suspend fun saveSession(user: UserModel) {
        userPreference.saveSession(user)
    }

    fun getSession(): Flow<UserModel> {
        return userPreference.getSession()
    }

    suspend fun logout() {
        userPreference.logout()
    }

    suspend fun registerUser(name: String, email: String, password: String): LiveData<Result<String>> {
        val result = MediatorLiveData<Result<String>>()
        result.value = Result.Loading

        try {
            val response = authApiService.register(name, email, password)
            var message: String

            if (response.isSuccessful) {
                val status = response.body()?.error ?: false
                message = response.body()?.message ?: "Unknown Message"
                appExecutors.mainThread.execute {
                    if(!status) {
                        result.value = Result.Success(message)
                    } else {
                        result.value = Result.Error(message)
                    }
                }
            } else {
                appExecutors.mainThread.execute {
                    val errorBody = Gson().fromJson(response.errorBody()?.string(), ErrorResponse::class.java)
                    if (errorBody.message != null) {
                        message = errorBody.message
                    } else {
                        message = response.message()
                    }

                    result.value = Result.Error("Registration failed: $message")
                }
            }
        } catch (e: Exception) {
            appExecutors.mainThread.execute {
                result.value = Result.Error("Registration failed: ${e.message}")
            }
        }

        return result
    }

    suspend fun loginUser(email: String, password: String): LiveData<Result<UserModel>> {
        val result = MediatorLiveData<Result<UserModel>>()
        result.value = Result.Loading

        try {
            val response = authApiService.login(email, password)

            if (response.isSuccessful) {
                val loginUser = response.body()?.loginResult
                val token = loginUser?.token ?: throw Exception("Token is null")
                val user = UserModel(
                    email = email,
                    token = token,
                    isLogin = true
                )
                appExecutors.mainThread.execute {
                    result.value = Result.Success(user)
                }
            } else {
                val errorBody = Gson().fromJson(response.errorBody()?.string(), ErrorResponse::class.java)
                val message = errorBody?.message ?: response.message()
                appExecutors.mainThread.execute {
                    result.value = Result.Error("Login failed: $message")
                }
            }
        } catch (e: Exception) {
            appExecutors.mainThread.execute {
                result.value = Result.Error("Login failed: ${e.message}")
            }
        }

        return result
    }

    companion object {
        @Volatile
        private var instance: UserRepository? = null
        fun getInstance(
            userPreference: UserPreference,
            authApiService: AuthApiService,
            appExecutors: AppExecutors
        ): UserRepository =
            instance ?: synchronized(this) {
                instance ?: UserRepository(userPreference, authApiService, appExecutors)
            }.also { instance = it }
    }
}