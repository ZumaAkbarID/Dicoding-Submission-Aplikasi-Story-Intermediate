package com.rwa.tellme.view.addnew

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rwa.tellme.data.Result
import com.rwa.tellme.data.remote.response.StoryUploadResponse
import com.rwa.tellme.data.repository.StoryRepository
import kotlinx.coroutines.launch
import java.io.File

class AddNewStoryViewModel(private val storyRepository: StoryRepository) : ViewModel() {
    private val _uploadResult = MutableLiveData<Result<StoryUploadResponse>>()
    val uploadResult: LiveData<Result<StoryUploadResponse>> = _uploadResult

    fun uploadStory(description: String, photoFile: File, lat: Float? = null, lon: Float? = null) {
        _uploadResult.value = Result.Loading
        viewModelScope.launch {
            val result = storyRepository.uploadStory(description, photoFile, lat, lon)
            _uploadResult.postValue(result)
        }
    }
}