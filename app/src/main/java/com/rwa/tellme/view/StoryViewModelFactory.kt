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
import com.rwa.tellme.view.main.MainStoryViewModel
import com.rwa.tellme.view.main.MainViewModel
import com.rwa.tellme.view.maps.MapsViewModel
import com.rwa.tellme.view.signup.SignupViewModel

class StoryViewModelFactory : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    class StoryViewModelFactory(
        private val storyRepository: StoryRepository
    ) : ViewModelProvider.NewInstanceFactory() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return when {
                modelClass.isAssignableFrom(MainStoryViewModel::class.java) -> {
                    MainStoryViewModel(storyRepository) as T
                }
                modelClass.isAssignableFrom(AddNewStoryViewModel::class.java) -> {
                    AddNewStoryViewModel(storyRepository) as T
                }
                modelClass.isAssignableFrom(MapsViewModel::class.java) -> {
                    MapsViewModel(storyRepository) as T
                }
                else -> throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
            }
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: StoryViewModelFactory? = null
        @JvmStatic
        fun getInstance(context: Context): StoryViewModelFactory {
            if (INSTANCE == null) {
                synchronized(StoryViewModelFactory::class.java) {
                    INSTANCE = StoryViewModelFactory(
                        StoryInjection.provideStoryRepository(context)
                    )
                }
            }
            return INSTANCE as StoryViewModelFactory
        }
    }
}