package com.yulia.workout

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView

class MainActivity : Activity() {

    private lateinit var mirror: ImageView
    private lateinit var streak: TextView
    private lateinit var doneButton: Button
    private val handler = Handler(Looper.getMainLooper())
    private var tick = 0

    private val animate = object : Runnable {
        override fun run() {
            val mood = Mood.now(this@MainActivity)
            tick = (tick + 1) % 3
            mirror.setImageResource(if (tick == 2) mood.frame2 else mood.frame1)
            handler.postDelayed(this, 900)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mirror = findViewById(R.id.mirror)
        streak = findViewById(R.id.streak)
        doneButton = findViewById(R.id.done)

        findViewById<Button>(R.id.open_workout).setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(WORKOUT_URL)))
        }
        doneButton.setOnClickListener {
            Streak.setDoneToday(this, !Streak.isDoneToday(this))
            render()
            MirrorWidget.refresh(this)
        }
    }

    override fun onResume() {
        super.onResume()
        render()
        handler.post(animate)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(animate)
    }

    private fun render() {
        val done = Streak.isDoneToday(this)
        val days = Streak.count(this)
        mirror.setImageResource(Mood.now(this).frame1)
        streak.text = resources.getQuantityString(R.plurals.streak_days, days, days)
        doneButton.text = getString(if (done) R.string.done_undo else R.string.mark_done)
        doneButton.isActivated = done
    }
}
