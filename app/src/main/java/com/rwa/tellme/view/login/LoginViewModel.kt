package com.rwa.tellme.view.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.rwa.tellme.data.Result
import com.rwa.tellme.data.repository.UserRepository
import com.rwa.tellme.data.model.UserModel
import com.rwa.tellme.data.remote.retrofit.StoryApiConfig
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: UserRepository) : ViewModel() {
    fun saveSession(user: UserModel) {
        viewModelScope.launch {
            repository.saveSession(user)
        }
    }

    fun getSession(): LiveData<UserModel> {
        return repository.getSession().asLiveData()
    }

    fun setTokenToInterceptor(token: String) {
        StoryApiConfig.initAuthInterceptor { token }
    }

    private val _loginResult = MutableLiveData<Result<UserModel>>()
    val loginResult: LiveData<Result<UserModel>> = _loginResult

    fun loginUser(email: String, password: String) {
        _loginResult.value = Result.Loading
        viewModelScope.launch {
            val result = repository.loginUser(email, password)
            result.observeForever { _loginResult.value = it }
        }
    }
}