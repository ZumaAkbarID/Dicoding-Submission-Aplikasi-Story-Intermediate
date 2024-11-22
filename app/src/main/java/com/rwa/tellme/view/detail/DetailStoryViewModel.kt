package com.rwa.tellme.view.detail

import androidx.lifecycle.ViewModel
import com.rwa.tellme.utils.formatDateToLocale

class DetailStoryViewModel : ViewModel() {
    fun formatCreatedAt(createdAt: String): String {
        return formatDateToLocale(createdAt)
    }
}