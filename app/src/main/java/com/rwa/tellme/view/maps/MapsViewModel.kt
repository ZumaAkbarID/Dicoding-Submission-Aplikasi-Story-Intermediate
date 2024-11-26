package com.rwa.tellme.view.maps

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rwa.tellme.data.Result
import com.rwa.tellme.data.model.StoryModel
import com.rwa.tellme.data.repository.StoryRepository
import kotlinx.coroutines.launch

class MapsViewModel(
    private val storyRepository: StoryRepository
) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>().apply {
        value = false
    }
    val isLoading: LiveData<Boolean> = _isLoading

    private val _listStory = MutableLiveData<List<StoryModel>>().apply {
        value = emptyList()
    }
    val listStory: LiveData<List<StoryModel>> = _listStory

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun showAllStoryWithLocation() {
        _isLoading.value = true
        viewModelScope.launch {
            storyRepository.getAllStoryWithLocation().observeForever { result ->
                when (result) {
                    is Result.Loading -> _isLoading.postValue(true)
                    is Result.Success -> {
                        _isLoading.postValue(false)
                        _listStory.postValue(result.data)
                    }
                    is Result.Error -> {
                        _isLoading.postValue(false)
                        _errorMessage.postValue(result.error)
                    }
                }
            }
        }
    }
}