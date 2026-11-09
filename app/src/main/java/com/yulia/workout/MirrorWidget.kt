package com.yulia.workout

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import java.time.ZoneId

class MirrorWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        refresh(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            ACTION_TICK, Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED -> refresh(context)
        }
    }

    override fun onDisabled(context: Context) {
        context.getSystemService(AlarmManager::class.java).cancel(tickIntent(context))
    }

    companion object {
        private const val ACTION_TICK = "com.yulia.workout.TICK"

        /** Redraws every widget on the home screen and books the next mood change. */
        fun refresh(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, MirrorWidget::class.java))
            if (ids.isEmpty()) return
            val mood = Mood.now(context)
            val streak = Streak.count(context)
            val open = PendingIntent.getActivity(
                context, 0, Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            for (id in ids) {
                val views = RemoteViews(context.packageName, R.layout.widget_mirror)
                views.setImageViewResource(R.id.frame_a, mood.frame1)
                views.setImageViewResource(R.id.frame_b, mood.frame1)
                views.setImageViewResource(R.id.frame_c, mood.frame2)
                views.setTextViewText(R.id.streak, streak.toString())
                views.setOnClickPendingIntent(R.id.root, open)
                manager.updateAppWidget(id, views)
            }
            scheduleNext(context)
        }

        private fun tickIntent(context: Context): PendingIntent = PendingIntent.getBroadcast(
            context, 1, Intent(context, MirrorWidget::class.java).setAction(ACTION_TICK),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        private fun scheduleNext(context: Context) {
            val at = Mood.nextChange().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            // An inexact window needs no special permission; within 5 minutes is fine here.
            context.getSystemService(AlarmManager::class.java)
                .setWindow(AlarmManager.RTC, at, 5 * 60 * 1000L, tickIntent(context))
        }
    }
}
