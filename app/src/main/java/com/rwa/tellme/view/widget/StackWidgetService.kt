package com.rwa.tellme.view.widget

import android.content.Intent
import android.widget.RemoteViewsService
import com.rwa.tellme.data.local.database.StoryDatabase
import com.rwa.tellme.data.remote.retrofit.StoryApiConfig
import com.rwa.tellme.data.repository.StoryRepository
import com.rwa.tellme.utils.AppExecutors

class StackWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        val database = StoryDatabase.getDatabase(baseContext)

        val storyRepository = StoryRepository.getInstance(
            StoryApiConfig.getProtectedService(),
            database,
            AppExecutors()
        )

        return StackRemoteViewsFactory(this.applicationContext, storyRepository)
    }
}