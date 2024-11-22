package com.rwa.tellme.view.widget

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.rwa.tellme.R
import com.rwa.tellme.data.repository.StoryRepository
import kotlinx.coroutines.runBlocking
import com.rwa.tellme.data.Result
import java.net.URL

internal class StackRemoteViewsFactory(
    private val mContext: Context,
    private val storyRepository: StoryRepository
) : RemoteViewsService.RemoteViewsFactory {

    private val mWidgetItems = ArrayList<RemoteViews>()

    override fun onCreate() {

    }

    override fun onDataSetChanged() {
        mWidgetItems.clear()

        val storiesResult = runBlocking {
            storyRepository.getAllStory().value
        }

        if (storiesResult is Result.Success) {
            storiesResult.data.forEach { story ->
                val bitmap = try {
                    val url = URL(story.photo)
                    BitmapFactory.decodeStream(url.openConnection().getInputStream())
                } catch (e: Exception) {
                    null
                }

                val rv = RemoteViews(mContext.packageName, R.layout.widget_item).apply {
                    bitmap?.let { setImageViewBitmap(R.id.imageView, it) }
                    setTextViewText(R.id.textView, story.name)
                }

                mWidgetItems.add(rv)
            }
        }
    }

    override fun onDestroy() {

    }

    override fun getCount(): Int = mWidgetItems.size

    override fun getViewAt(position: Int): RemoteViews {
        return mWidgetItems[position]
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(i: Int): Long = 0

    override fun hasStableIds(): Boolean = false
}
