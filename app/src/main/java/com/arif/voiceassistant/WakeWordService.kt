
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

class WakeWordService : Service() {

    private var porcupineManager: PorcupineManager? = null

    companion object {
        // Replace this placeholder with your own Picovoice AccessKey.
        // Never share your real AccessKey publicly.
        private const val ACCESS_KEY =
            "PASTE_YOUR_PICOVOICE_ACCESS_KEY_HERE"

        private const val CHANNEL_ID = "arif_wake_word"
        private const val NOTIFICATION_ID = 2401
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        // Avoid starting Porcupine more than once.
        if (porcupineManager != null) {
            updateNotification("Wake-word service is already running.")
            return START_NOT_STICKY
        }

        try {
            val notification = buildNotification(
                "Starting voice assistant..."
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }

            if (ACCESS_KEY.startsWith("PASTE_")) {
                updateNotification(
                    "Setup needed: add your Picovoice AccessKey."
                )

                // Keep the service alive for notification testing.
                return START_NOT_STICKY
            }

            porcupineManager = PorcupineManager.Builder()
                .setAccessKey(ACCESS_KEY)
                .setKeywordPaths(
                    arrayOf("hello_assistant_android.ppn")
                )
                .build(
                    this,
                    PorcupineManagerCallback {
                        updateNotification(
                            "Wake word detected! Open the app to continue."
                        )
                    }
                )

            porcupineManager?.start()

            updateNotification(
                "Listening for Hello Assistant."
            )

        } catch (e: Exception) {
            updateNotification(
                "Could not start listening. Check AccessKey, model file and permissions."
            )

            releasePorcupine()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        return START_NOT_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Voice Assistant",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description =
                    "Shows the status of wake-word listening."
            }

            val notificationManager =
                getSystemService(NotificationManager::class.java)

            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(message: String): Notification {
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        }

        return builder
            .setContentTitle("Arif Voice Assistant")
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(message: String) {
        val notificationManager =
            getSystemService(NotificationManager::class.java)

        notificationManager.notify(
            NOTIFICATION_ID,
            buildNotification(message)
        )
    }

    private fun releasePorcupine() {
        try {
            porcupineManager?.stop()
        } catch (_: Exception) {
            // Ignore cleanup errors.
        }

        try {
            porcupineManager?.delete()
        } catch (_: Exception) {
            // Ignore cleanup errors.
        }

        porcupineManager = null
    }

    override fun onDestroy() {
        releasePorcupine()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }

        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
