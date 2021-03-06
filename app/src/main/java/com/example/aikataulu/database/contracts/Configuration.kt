package com.example.aikataulu.database.contracts

import android.database.Cursor
import android.provider.BaseColumns
import com.example.aikataulu.models.TimetableConfiguration

object ConfigurationContract {
    const val SQL_CREATE_ENTRIES =
        "CREATE TABLE ${ConfigurationEntry.TABLE_NAME} (" +
                "${ConfigurationEntry.COLUMN_NAME_WIDGET_ID} INTEGER PRIMARY KEY," +
                "${ConfigurationEntry.COLUMN_NAME_UPDATE_INTERVAL_SECONDS} INTEGER, " +
                "${ConfigurationEntry.COLUMN_NAME_SELECTED_STOP_ID} INTEGER," +
                "${ConfigurationEntry.COLUMN_NAME_AUTO_UPDATE_ENABLED} INTEGER, " +
                "${ConfigurationEntry.COLUMN_NAME_WIDGET_ENABLED} INTEGER DEFAULT 0)"

    const val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS ${ConfigurationEntry.TABLE_NAME}"

    object ConfigurationEntry : BaseColumns {
        const val TABLE_NAME = "configurations"
        const val COLUMN_NAME_WIDGET_ID = "widgetid"
        const val COLUMN_NAME_UPDATE_INTERVAL_SECONDS = "updateinterval"
        const val COLUMN_NAME_SELECTED_STOP_ID = "selectedstop"
        const val COLUMN_NAME_AUTO_UPDATE_ENABLED = "autoupdate"
        const val COLUMN_NAME_WIDGET_ENABLED = "widgetenabled"
        fun allColumns(): Array<String> {
            return arrayOf(
                COLUMN_NAME_WIDGET_ID,
                COLUMN_NAME_UPDATE_INTERVAL_SECONDS,
                COLUMN_NAME_SELECTED_STOP_ID,
                COLUMN_NAME_AUTO_UPDATE_ENABLED,
                COLUMN_NAME_WIDGET_ENABLED
            )
        }

        fun cursorToPoco(cursor: Cursor?): TimetableConfiguration? {
            return if (cursor != null) {
                val entry = ConfigurationEntry
                val updateIntervalS =
                    cursor.getInt(cursor.getColumnIndex(entry.COLUMN_NAME_UPDATE_INTERVAL_SECONDS))
                val stopId =
                    cursor.getString(cursor.getColumnIndex(entry.COLUMN_NAME_SELECTED_STOP_ID))
                val isAutoUpdateEnabled =
                    cursor.getInt(cursor.getColumnIndex(entry.COLUMN_NAME_AUTO_UPDATE_ENABLED)) == 1
                val widgetId =
                    cursor.getInt(cursor.getColumnIndex(entry.COLUMN_NAME_WIDGET_ID))
                val isWidgetEnabled =
                    cursor.getInt(cursor.getColumnIndex(entry.COLUMN_NAME_WIDGET_ENABLED)) == 1
                TimetableConfiguration(
                    updateIntervalS,
                    stopId,
                    isAutoUpdateEnabled,
                    widgetId,
                    null,
                    isWidgetEnabled
                )
            } else null
        }
    }
}
