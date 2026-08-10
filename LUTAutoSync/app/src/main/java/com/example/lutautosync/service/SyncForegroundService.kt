package com.example.lutautosync.service

import android.app.*
import android.content.Intent
import android.os.*
import androidx.core.app.NotificationCompat
import com.example.lutautosync.data.*
import kotlinx.coroutines.*
import java.io.File

class SyncForegroundService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var observer: FileObserver? = null

    override fun onCreate() {
        super.onCreate()
        createChannel()
        startForeground(7, notification("Monitoring started"))
        val root = File(Environment.getExternalStorageDirectory(), Environment.DIRECTORY_DCIM)
        startWatching(getSharedPreferences("settings", MODE_PRIVATE).getString("watch", root.path)!!)
    }

    private fun startWatching(path: String) {
        observer?.stopWatching()
        observer = object : FileObserver(path, FileObserver.CREATE or FileObserver.MOVED_TO) {
            override fun onEvent(event: Int, name: String?) {
                if (name == null) return
                val file = File(path, name)
                if (file.isFile && file.extension.lowercase() in setOf("jpg", "jpeg", "png", "heif")) scope.launch { handle(file) }
            }
        }.also { it.startWatching() }
    }

    private suspend fun handle(file: File) {
        val lut = AppContainer.db.lutDao().default() ?: return
        val md5 = com.example.lutautosync.processing.ImageProcessor(contentResolver).md5(file)
        if (AppContainer.db.processedDao().exists(file.path, md5, lut.id)) return
        AppContainer.db.processedDao().insert(ProcessedFileEntity(path = file.path, md5 = md5, processedAt = System.currentTimeMillis(), lutId = lut.id))
        updateNotification("Recorded: ${file.name}")
    }

    private fun updateNotification(text: String) { getSystemService(NotificationManager::class.java).notify(7, notification(text)) }
    private fun notification(text: String) = NotificationCompat.Builder(this, "sync").setSmallIcon(android.R.drawable.ic_menu_gallery).setContentTitle("LUT Auto Sync").setContentText(text).setOngoing(true).build()
    private fun createChannel() { getSystemService(NotificationManager::class.java).createNotificationChannel(NotificationChannel("sync", "LUT Sync", NotificationManager.IMPORTANCE_LOW)) }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int) = START_STICKY
    override fun onDestroy() { observer?.stopWatching(); scope.cancel(); super.onDestroy() }
    override fun onBind(intent: Intent?) = null
}
