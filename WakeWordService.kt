package com.arif.voiceassistant

import ai.picovoice.porcupine.PorcupineManager
import ai.picovoice.porcupine.PorcupineManagerCallback
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.widget.Toast

class WakeWordService : Service() {
    private var manager: PorcupineManager? = null

    companion object {
        // Add your own Picovoice AccessKey locally. Do not share it publicly.
        private const val ACCESS_KEY = "PASTE_YOUR_PICOVOICE_ACCESS_KEY_HERE"
        private const val CHANNEL_ID = "arif_wake_word"
        private const val NOTIFICATION_ID = 2401
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = buildNotification("Listening for wake word while this service is active")
        if (Build.VERSION.SDK_INT >= 29) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        if (ACCESS_KEY.startsWith("PASTE_")) {
            updateNotification("Setup needed: add your Picovoice AccessKey and wake-word model.")
            stopSelf()
            return START_NOT_STICKY
        }

        try {
            // Put a custom Android keyword model at:
            // app/src/main/assets/hello_assistant_android.ppn
            manager = PorcupineManager.Builder()
                .setAccessKey(ACCESS_KEY)
                .setKeywordPaths(arrayOf("hello_assistant_android.ppn"))
                .build(this, PorcupineManagerCallback {
                    updateNotification("Wake word detected. Open Arif Voice Assistant to give a command.")
                    // Android restricts background activity launches. Use the notification/app
                    // to continue into speech recognition rather than launching UI silently.
                })
            manager?.start()
            updateNotification("Listening for “Hello Assistant”. Tap the app to stop or configure.")
        } catch (e: Exception) {
            updateNotification("Wake-word setup failed. Check AccessKey and model file.")
            stopSelf()
        }
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Arif Voice Assistant microphone",
                NotificationManager.IMPORTANCE_LOW
            )
            channel.description = "Visible status while wake-word listening is enabled"
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun buildNotification(text: String): Notification {
        return if (Build.VERSION.SDK_INT >= 26) {
            Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("Arif Voice Assistant")
                .setContentText(text)
                .setSmallIcon(android.R.drawable.ic_btn_speak_now)
                .setOngoing(true)
                .build()
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
                .setContentTitle("Arif Voice Assistant")
                .setContentText(text)
                .setSmallIcon(android.R.drawable.ic_btn_speak_now)
                .setOngoing(true)
                .build()
        }
    }

    private fun updateNotification(text: String) {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, buildNotification(text))
    }

    override fun onDestroy() {
        try { manager?.stop() } catch (_: Exception) {}
        manager?.delete()
        manager = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
