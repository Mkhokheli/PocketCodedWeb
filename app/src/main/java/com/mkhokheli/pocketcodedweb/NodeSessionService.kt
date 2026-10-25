package com.mkhokheli.pocketcodedweb

import android.app.Service
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.Process
import java.io.File
import org.json.JSONObject

/** A separate process is required: Node Mobile can only start once per process. */
class NodeSessionService : Service() {
    private var launched = false

    override fun onCreate() {
        super.onCreate()
        val channel = "node-development-server"
        if (Build.VERSION.SDK_INT >= 26) {
            getSystemService(NotificationManager::class.java).createNotificationChannel(
                NotificationChannel(channel, "Development server", NotificationManager.IMPORTANCE_LOW),
            )
        }
        val open = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val stop = PendingIntent.getService(this, 1, Intent(this, NodeSessionService::class.java).setAction("stop"), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val builder = if (Build.VERSION.SDK_INT >= 26) Notification.Builder(this, channel) else Notification.Builder(this)
        startForeground(42, builder
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle("PocketCoded Web server")
            .setContentText("Your Node session is running for browser preview.")
            .setContentIntent(open)
            .setOngoing(true)
            .addAction(Notification.Action.Builder(null, "Stop", stop).build())
            .build())
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "stop") {
            stopSelf()
            return START_NOT_STICKY
        }
        if (launched) return START_NOT_STICKY
        launched = true
        val directory = File(noBackupFilesDir, "node-session")
        Thread({
            try {
                System.loadLibrary("node")
                System.loadLibrary("pocketcoded-node")
                val config = JSONObject(File(directory, "config.json").readText())
                if (config.optBoolean("browserPreview", false) &&
                    DeviceEfficiency.budget.terminalRenderLines != Int.MAX_VALUE) {
                    // Leave interactive browser rendering more CPU headroom while
                    // keeping the development server alive in the foreground.
                    Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND)
                }
                val arguments = mutableListOf("node")
                if (config.optString("mode") == "tests") arguments += "--test-reporter=tap"
                arguments += File(directory, "session.cjs").absolutePath
                arguments += File(directory, "config.json").absolutePath
                val code = NativeNodeBridge.start(arguments.toTypedArray())
                File(directory, "status.json").writeText(JSONObject()
                    .put("state", if (code == 0) "exited" else "failed")
                    .put("exitCode", code).toString())
            } catch (error: Throwable) {
                File(directory, "session.log").appendText("\n${error.message ?: error.javaClass.simpleName}\n")
                File(directory, "status.json").writeText(JSONObject().put("state", "failed").toString())
            } finally {
                stopSelf()
            }
        }, "pocketcoded-node-session").start()
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Process.killProcess(Process.myPid())
    }
}
