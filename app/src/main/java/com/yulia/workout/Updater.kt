package com.yulia.workout

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.core.content.FileProvider
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/**
 * Updates the app from the repo's GitHub releases. Each release is tagged build-N, where N is the
 * build number baked into that APK as its versionCode, so a bigger N means a newer app.
 */
object Updater {
    private const val LATEST = "https://api.github.com/repos/dmeltz/Yulia_workout/releases/latest"

    data class Release(val build: Int, val apkUrl: String)

    /** The newest published release, or null when it can't be reached. Call off the main thread. */
    fun latest(): Release? = try {
        val conn = (URL(LATEST).openConnection() as HttpURLConnection).apply {
            setRequestProperty("Accept", "application/vnd.github+json")
            connectTimeout = 10_000
            readTimeout = 10_000
        }
        try {
            if (conn.responseCode != 200) null else {
                val json = JSONObject(conn.inputStream.bufferedReader().use { it.readText() })
                val build = json.getString("tag_name").removePrefix("build-").toIntOrNull()
                val assets = json.getJSONArray("assets")
                val apk = (0 until assets.length()).map { assets.getJSONObject(it) }
                    .firstOrNull { it.getString("name").endsWith(".apk") }
                if (build == null || apk == null) null else Release(build, apk.getString("browser_download_url"))
            }
        } finally {
            conn.disconnect()
        }
    } catch (e: Exception) {
        null
    }

    /** Downloads the APK into the app's cache. Call off the main thread. */
    fun download(context: Context, release: Release): File? = try {
        val dir = File(context.cacheDir, "updates").apply { mkdirs() }
        val file = File(dir, "update.apk")
        var conn = URL(release.apkUrl).openConnection() as HttpURLConnection
        // GitHub sends the file from another host; follow the redirects by hand to be safe.
        var hops = 0
        while (conn.responseCode in 301..308 && hops++ < 5) {
            val next = conn.getHeaderField("Location")
            conn.disconnect()
            conn = URL(next).openConnection() as HttpURLConnection
        }
        try {
            if (conn.responseCode != 200) null else {
                conn.inputStream.use { input -> file.outputStream().use { input.copyTo(it) } }
                file
            }
        } finally {
            conn.disconnect()
        }
    } catch (e: Exception) {
        null
    }

    /** True when Android lets this app install updates. Otherwise opens the setting and returns false. */
    fun ensureInstallAllowed(activity: Activity): Boolean {
        if (activity.packageManager.canRequestPackageInstalls()) return true
        activity.startActivity(
            Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${activity.packageName}"))
        )
        return false
    }

    /** Hands the downloaded APK to Android's installer, which asks to confirm. */
    fun install(activity: Activity, apk: File) {
        val uri = FileProvider.getUriForFile(activity, "${activity.packageName}.files", apk)
        activity.startActivity(
            Intent(Intent.ACTION_VIEW)
                .setDataAndType(uri, "application/vnd.android.package-archive")
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}
