package com.example.aikataulu;

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import com.example.aikataulu.models.Departure
import com.example.aikataulu.ui.ConfigurationActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class TimetableWidgetProvider : AppWidgetProvider() {
    companion object {
        private const val TAG = "TIMETABLE.WidgetProvider"

        // Send data to be handled by ViewFactory
        fun sendUpdateWidgetBroadcast(context: Context, widgetId: Int) {
            context.sendBroadcast(Intent(context, TimetableWidgetProvider::class.java)
                .setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId))
        }
    }

    override fun onEnabled(context: Context?) {
        Log.d(TAG, "onEnabled()")
        // Start service when a widget has been added
        context!!.startService(Intent(context, TimetableService::class.java))
        val widgetManager = AppWidgetManager.getInstance(context)
        val widgetIds = widgetManager.getAppWidgetIds(ComponentName(context.packageName, TimetableWidgetProvider::class.qualifiedName!!))
        // Perform this loop procedure for each App Widget that belongs to this provider
        widgetIds.forEach { widgetId ->
            Log.i(TAG, "Attaching click handler to widget id $widgetId")
            widgetManager.updateAppWidget(widgetId, RemoteViews(context.packageName, R.layout.widget).apply {
                setOnClickPendingIntent(R.id.widgetContainer, Intent(context, ConfigurationActivity::class.java)
                    .let { intent ->
                        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                        intent.action = "configure_widget-$widgetId"
                        PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
                    })
            })
        }
        super.onEnabled(context)
    }

    // Handle receiving of data from service
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            val widgetId = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
            AppWidgetManager.getInstance(context).apply {
                updateAppWidget(
                    widgetId,
                    RemoteViews(context!!.packageName, R.layout.widget).apply {
                        setRemoteAdapter(
                            R.id.widget_content_target,
                            Intent(context, TimetableRemoteViewsService::class.java)
                        )
                    })
                notifyAppWidgetViewDataChanged(widgetId, R.id.widget_content_target)
            }
        }
        super.onReceive(context, intent)
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        Log.i(TAG, "onUpdate()")
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    override fun onDeleted(context: Context?, appWidgetIds: IntArray?) {
        // Only stop the service if this is the only added widget
        if (context != null && (appWidgetIds == null || appWidgetIds.size <= 1)) {
            Log.i(TAG, "Stopping service...")
            context.stopService(Intent(context, TimetableService::class.java))
        } else Log.w(TAG, "Context is null when widget provider is deleted. Cannot stop service.")
        super.onDeleted(context, appWidgetIds)
    }
}