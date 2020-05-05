package com.example.aikataulu

import android.app.NotificationManager
import android.app.Service
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.aikataulu.api.Api
import com.example.aikataulu.models.Departure
import com.example.aikataulu.models.Timetable
import com.example.aikataulu.ui.MainActivity
import com.google.gson.Gson
import java.util.*
import kotlin.collections.HashMap

class TimetableService : Service() {
    private val _timerTasks = HashMap<Int, TimerTask>()
    private val _timers = HashMap<Int, Timer>()

    companion object {
        private const val TAG = "TIMETABLE.Service"
    }

    private fun ensureTimedTaskCanceled(widgetId: Int) {
        _timerTasks.filter { it.key == widgetId }.forEach { it.value.cancel() }
    }

    private fun setAutoUpdate(widgetId: Int, b: Boolean) {
        ensureTimedTaskCanceled(widgetId)
        val config = TimetableConfiguration.loadConfigForWidget(applicationContext, widgetId)
        val stopName = config.stopName
        if (b && stopName != null) {
            val stops = Api.getStopsContainingText(stopName)
            if (stops.any()) {
                val stop = stops.first()
                _timerTasks[widgetId] = object: TimerTask() {
                    override fun run() {
                        val departures = Api.getDeparturesForStopId(stop.hrtId).map { Departure(it) }
                        Log.d(TAG, "Received ${departures.count()} departures for stop ${stop.name} (${stop.hrtId})")

                        // Update content
                        applicationContext.contentResolver.update(TimetableDataProvider.CONTENT_URI,
                            ContentValues().apply{ put(TimetableDataProvider.COLUMN_TIMETABLE, Gson().toJson(Timetable(stop, departures))) },
                            stop.hrtId,
                            emptyArray<String>()
                        )
                        // Notify WidgetProvider of the changes
                        TimetableWidgetProvider.sendUpdateWidgetBroadcast(applicationContext, widgetId)
                    }
                }
                _timers[widgetId] = Timer()
                _timers[widgetId]!!.scheduleAtFixedRate(_timerTasks[widgetId], 0, (1000 * config.updateIntervalS).toLong())
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "Received onStartCommand with intent name ${intent?.action}")
        val widgetIds = TimetableWidgetProvider.getExistingWidgetIds(applicationContext)
        TimetableConfiguration.cleanConfigFile(applicationContext, widgetIds)
        val configs = TimetableConfiguration.loadConfigForWidgets(applicationContext, widgetIds)
        configs.forEach {
            val widgetId = it.key
            val config = it.value
            ensureTimedTaskCanceled(widgetId)
            setAutoUpdate(widgetId, config.autoUpdate)
        }

        val builder = NotificationCompat
            .Builder(this, MainActivity.notificationChannelId(getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager))
            .setSmallIcon(R.mipmap.icon)
            .setContentTitle("Timetable")
            .setContentText("Service was created.")
            .setPriority(NotificationCompat.PRIORITY_MIN)

        startForeground(944, builder.build())

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate() {
        Log.d(TAG, "onCreate()")
        super.onCreate()
    }

    override fun onDestroy() {
        _timerTasks.forEach { it.value.cancel() }
        Log.d(TAG, "onDestroy()")
        super.onDestroy()
    }
}