package com.yulia.workout

import android.content.Context
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

const val WORKOUT_URL = "https://my.schooler.biz/s/87955/login"

/** The five pictures in the mirror. Each has two frames that the widget flips between. */
enum class Mood(val frame1: Int, val frame2: Int) {
    HAPPY(R.drawable.mirror_happy_1, R.drawable.mirror_happy_2),
    IMPATIENT(R.drawable.mirror_impatient_1, R.drawable.mirror_impatient_2),
    ANGRY(R.drawable.mirror_angry_1, R.drawable.mirror_angry_2),
    SAD(R.drawable.mirror_sad_1, R.drawable.mirror_sad_2),
    DONE(R.drawable.mirror_done_1, R.drawable.mirror_done_2);

    companion object {
        fun now(context: Context, time: LocalTime = LocalTime.now()): Mood = when {
            Streak.isDoneToday(context) -> DONE
            time.hour < 12 -> HAPPY
            time.hour < 17 -> IMPATIENT
            time.hour < 21 -> ANGRY
            else -> SAD
        }

        /** The next time the picture should change: 12:00, 17:00, 21:00 or midnight. */
        fun nextChange(now: LocalDateTime = LocalDateTime.now()): LocalDateTime {
            val today = now.toLocalDate()
            for (h in listOf(12, 17, 21)) {
                val t = today.atTime(h, 0)
                if (t.isAfter(now)) return t
            }
            return today.plusDays(1).atStartOfDay().plusMinutes(1)
        }
    }
}

/** Remembers which days the workout was done, on the phone only. */
object Streak {
    private const val PREFS = "workout"
    private const val KEY = "done_dates"

    private fun dates(context: Context): Set<String> =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getStringSet(KEY, emptySet())!!.toSet()

    fun isDoneToday(context: Context): Boolean = dates(context).contains(LocalDate.now().toString())

    fun setDoneToday(context: Context, done: Boolean) {
        val set = dates(context).toMutableSet()
        val today = LocalDate.now().toString()
        if (done) set.add(today) else set.remove(today)
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putStringSet(KEY, set).apply()
    }

    /** Days in a row. Today counts once it's done; until then the streak from yesterday still stands. */
    fun count(context: Context): Int {
        val set = dates(context)
        var day = LocalDate.now()
        if (!set.contains(day.toString())) day = day.minusDays(1)
        var n = 0
        while (set.contains(day.toString())) {
            n++
            day = day.minusDays(1)
        }
        return n
    }
}
