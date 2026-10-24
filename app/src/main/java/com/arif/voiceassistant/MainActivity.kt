
package com.arif.voiceassistant

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {

    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createScreen()
        checkPermissions()
    }

    private fun createScreen() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(28, 36, 28, 28)
            setBackgroundColor(0xFF101827.toInt())
        }

        val title = TextView(this).apply {
            text = "Arif Voice Assistant"
            textSize = 26f
            gravity = Gravity.CENTER
            setTextColor(0xFFFFFFFF.toInt())
        }

        status = TextView(this).apply {
            text = "Ready. Check permissions, then start the service."
            textSize = 16f
            gravity = Gravity.CENTER
            setTextColor(0xFFB9C7D9.toInt())
            setPadding(0, 24, 0, 24)
        }

        val startButton = Button(this).apply {
            text = "Start wake-word service"
            setOnClickListener {
                startWakeService()
            }
        }

        val stopButton = Button(this).apply {
            text = "Stop listening"
            setOnClickListener {
                stopWakeService()
            }
        }

        layout.addView(title)
        layout.addView(status)
        layout.addView(startButton)
        layout.addView(stopButton)

        setContentView(layout)
    }

    private fun checkPermissions() {
        val missing = mutableListOf<String>()

        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            missing.add(Manifest.permission.RECORD_AUDIO)
        }

        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            missing.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        if (missing.isNotEmpty()) {
            status.text = "Please allow microphone and notification permissions."
            requestPermissions(missing.toTypedArray(), 101)
        } else {
            status.text = "Permissions checked. Ready to start."
        }
    }

    private fun startWakeService() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            status.text = "Microphone permission is missing. Allow it first."
            requestPermissions(
                arrayOf(Manifest.permission.RECORD_AUDIO),
                101
            )
            return
        }

        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            status.text = "Notification permission is missing."
            requestPermissions(
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                102
            )
            return
        }

        try {
            val serviceIntent = Intent(
                this,
                WakeWordService::class.java
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }

            status.text =
                "Service request sent. If there is no notification, check the service error."
        } catch (e: Exception) {
            status.text =
                "Start error: ${e.javaClass.simpleName}\n${e.message ?: "No details available"}"
        }
    }

    private fun stopWakeService() {
        try {
            val stopped = stopService(
                Intent(this, WakeWordService::class.java)
            )

            status.text = if (stopped) {
                "Wake-word service stopped."
            } else {
                "The service was not running."
            }
        } catch (e: Exception) {
            status.text =
                "Stop error: ${e.javaClass.simpleName}\n${e.message ?: "No details available"}"
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )

        val microphoneGranted =
            checkSelfPermission(Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED

        val notificationsGranted =
            Build.VERSION.SDK_INT < 33 ||
                checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED

        status.text = when {
            !microphoneGranted ->
                "Microphone permission denied. Enable it in App settings."

            !notificationsGranted ->
                "Notification permission denied. Enable it in App settings."

            else ->
                "Permissions granted. Tap Start wake-word service."
        }
    }
}
