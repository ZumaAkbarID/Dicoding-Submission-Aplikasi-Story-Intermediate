package com.rwa.tellme.view

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rwa.tellme.data.repository.StoryRepository
import com.rwa.tellme.data.repository.UserRepository
import com.rwa.tellme.di.AuthInjection
import com.rwa.tellme.di.StoryInjection
import com.rwa.tellme.view.addnew.AddNewStoryViewModel
import com.rwa.tellme.view.login.LoginViewModel
import com.rwa.tellme.view.main.MainViewModel
import com.rwa.tellme.view.signup.SignupViewModel

class ViewModelFactory : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    class ViewModelFactory(
        private val userRepository: UserRepository
    ) : ViewModelProvider.NewInstanceFactory() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return when {
                modelClass.isAssignableFrom(MainViewModel::class.java) -> {
                    MainViewModel(userRepository) as T
                }
                modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                    LoginViewModel(userRepository) as T
                }
                modelClass.isAssignableFrom(SignupViewModel::class.java) -> {
                    SignupViewModel(userRepository) as T
                }
                else -> throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
            }
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: ViewModelFactory? = null
        @JvmStatic
        fun getInstance(context: Context): ViewModelFactory {
            if (INSTANCE == null) {
                synchronized(ViewModelFactory::class.java) {
                    INSTANCE = ViewModelFactory(
                        AuthInjection.provideRepository(context)
                    )
                }
            }
            return INSTANCE as ViewModelFactory
        }
    }
}