package com.rwa.tellme.view.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.rwa.tellme.R
import com.rwa.tellme.view.detail.DetailStoryActivity

/**
 * Implementation of App Widget functionality.
 */
class StoryWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    val views = RemoteViews(context.packageName, R.layout.story_widget)

    val intent = Intent(context, StackWidgetService::class.java)
    views.setRemoteAdapter(R.id.widget_item_layout, intent)

    val clickIntent = Intent(context, DetailStoryActivity::class.java)
    val pendingIntent = PendingIntent.getActivity(context, 0, clickIntent, PendingIntent.FLAG_IMMUTABLE)
    views.setPendingIntentTemplate(R.id.widget_item_layout, pendingIntent)

    appWidgetManager.updateAppWidget(appWidgetId, views)
}