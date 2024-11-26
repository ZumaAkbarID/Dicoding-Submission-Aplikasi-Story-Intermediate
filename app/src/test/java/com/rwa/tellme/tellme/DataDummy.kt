package com.rwa.tellme.tellme

import com.rwa.tellme.data.model.StoryModel

object DataDummy {
    fun generateDummyStoryResponse(): List<StoryModel> {
        val items: MutableList<StoryModel> = arrayListOf()
        for (i in 0..100) {
            val quote = StoryModel(
                i.toString(),
                "name + $i",
                "photo $i",
                "description $i",
                "created_at $i",
                i.toDouble(),
                (i + i).toDouble(),
            )
            items.add(quote)
        }
        return items
    }
}