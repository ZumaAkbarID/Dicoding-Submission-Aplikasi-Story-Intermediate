package com.rwa.tellme.view.widget

import android.content.Intent
import android.widget.RemoteViewsService
import com.rwa.tellme.data.pref.UserPreference
import com.rwa.tellme.data.pref.dataStore
import com.rwa.tellme.data.remote.retrofit.ApiConfig
import com.rwa.tellme.data.repository.StoryRepository
import com.rwa.tellme.utils.AppExecutors
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class StackWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        val pref = UserPreference.getInstance(baseContext.dataStore)
        val user = runBlocking { pref.getSession().first() }

        val storyRepository = StoryRepository.getInstance(
            ApiConfig.getProtectedService(user.token),
            AppExecutors()
        )

        return StackRemoteViewsFactory(this.applicationContext, storyRepository)
    }
}