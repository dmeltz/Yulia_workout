package com.yulia.workout

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import java.io.File

class MainActivity : Activity() {

    private lateinit var mirror: ImageView
    private lateinit var streak: TextView
    private lateinit var doneButton: Button
    private lateinit var updateButton: TextView
    private val handler = Handler(Looper.getMainLooper())
    private var tick = 0
    private var busy = false
    private var waitingToInstall: File? = null

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
        updateButton = findViewById(R.id.check_update)

        findViewById<Button>(R.id.open_workout).setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(WORKOUT_URL)))
        }
        doneButton.setOnClickListener {
            Streak.setDoneToday(this, !Streak.isDoneToday(this))
            render()
            MirrorWidget.refresh(this)
        }
        updateButton.setOnClickListener { checkForUpdate(manual = true) }
        showVersion()
        checkForUpdate(manual = false)
    }

    override fun onResume() {
        super.onResume()
        render()
        handler.post(animate)
        // Back from the "allow installs" setting: carry on with the update.
        waitingToInstall?.let { apk ->
            if (packageManager.canRequestPackageInstalls()) {
                waitingToInstall = null
                Updater.install(this, apk)
            }
        }
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

    private fun showVersion() {
        updateButton.text = getString(R.string.version_check, BuildConfig.VERSION_NAME)
    }

    private fun checkForUpdate(manual: Boolean) {
        if (busy) return
        busy = true
        if (manual) updateButton.text = getString(R.string.checking)
        Thread {
            val release = Updater.latest()
            runOnUiThread {
                busy = false
                showVersion()
                if (isFinishing) return@runOnUiThread
                when {
                    release == null -> if (manual) toast(R.string.update_check_failed)
                    release.build > BuildConfig.VERSION_CODE -> offerUpdate(release)
                    manual -> toast(R.string.up_to_date)
                }
            }
        }.start()
    }

    private fun offerUpdate(release: Updater.Release) {
        AlertDialog.Builder(this)
            .setTitle(R.string.update_title)
            .setMessage(getString(R.string.update_message, "1.${release.build}"))
            .setPositiveButton(R.string.update_now) { _, _ -> downloadAndInstall(release) }
            .setNegativeButton(R.string.later, null)
            .show()
    }

    private fun downloadAndInstall(release: Updater.Release) {
        busy = true
        updateButton.text = getString(R.string.downloading)
        Thread {
            val apk = Updater.download(this, release)
            runOnUiThread {
                busy = false
                showVersion()
                if (apk == null) {
                    toast(R.string.download_failed)
                } else if (Updater.ensureInstallAllowed(this)) {
                    Updater.install(this, apk)
                } else {
                    waitingToInstall = apk
                    toast(R.string.allow_installs)
                }
            }
        }.start()
    }

    private fun toast(text: Int) = Toast.makeText(this, text, Toast.LENGTH_LONG).show()
}
