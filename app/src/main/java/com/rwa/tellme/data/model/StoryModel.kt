package com.rwa.tellme.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class StoryModel(
    val id: String,
    val name: String,
    val photo: String,
    val description: String,
    val createdAt: String,
    val lon: Double,
    val lat: Double,
) : Parcelable
