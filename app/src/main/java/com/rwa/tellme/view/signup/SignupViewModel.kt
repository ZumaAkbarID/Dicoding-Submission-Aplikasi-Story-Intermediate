package com.rwa.tellme.view.signup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rwa.tellme.data.Result
import com.rwa.tellme.data.repository.UserRepository
import kotlinx.coroutines.launch

class SignupViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _signupResult = MutableLiveData<Result<String>>()
    val signupResult: LiveData<Result<String>> = _signupResult

    fun registerUser(name: String, email: String, password: String) {
        _signupResult.value = Result.Loading
        viewModelScope.launch {
            val result = userRepository.registerUser(name, email, password)
            result.observeForever { _signupResult.value = it }
        }
    }
}
