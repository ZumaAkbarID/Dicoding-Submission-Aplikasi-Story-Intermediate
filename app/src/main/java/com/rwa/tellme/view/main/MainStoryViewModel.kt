package com.rwa.tellme.view.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.rwa.tellme.data.Result
import com.rwa.tellme.data.model.StoryModel
import com.rwa.tellme.data.remote.response.StoryResponse
import com.rwa.tellme.data.repository.StoryRepository
import kotlinx.coroutines.launch

class MainStoryViewModel(
    private val storyRepository: StoryRepository
) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>().apply {
        value = false
    }
    val isLoading: LiveData<Boolean> = _isLoading

    fun setErrorMessage(message: String?) {
        _errorMessage.value = message
    }

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    val story: LiveData<PagingData<StoryModel>> = liveData {
        _isLoading.value = true
        try {
            val data = storyRepository.getAllStoryWithPager()
                .map { pagingData ->
                    pagingData.map { storyEntity ->
                        StoryModel(
                            id = storyEntity.id,
                            name = storyEntity.name,
                            description = storyEntity.description,
                            photo = storyEntity.photo,
                            createdAt = storyEntity.createdAt,
                            lon = storyEntity.lon,
                            lat = storyEntity.lat,
                        )
                    }
                }
                .cachedIn(viewModelScope)
            emitSource(data)
        } catch (exception: Exception) {
            _errorMessage.value = exception.message
        } finally {
            _isLoading.value = false
        }
    }

}