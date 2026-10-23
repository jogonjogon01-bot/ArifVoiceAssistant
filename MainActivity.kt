package com.arif.voiceassistant

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.view.Gravity

class MainActivity : Activity() {
    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
            text = "Wake-word mode needs your local AccessKey and model file."
            textSize = 16f
            gravity = Gravity.CENTER
            setTextColor(0xFFB9C7D9.toInt())
            setPadding(0, 24, 0, 24)
        }
        val start = Button(this).apply {
            text = "Start wake-word service"
            setOnClickListener { startWakeService() }
        }
        val stop = Button(this).apply {
            text = "Stop listening"
            setOnClickListener {
                stopService(Intent(this@MainActivity, WakeWordService::class.java))
                status.text = "Wake-word service stopped."
            }
        }
        layout.addView(title)
        layout.addView(status)
        layout.addView(start)
        layout.addView(stop)
        setContentView(layout)

        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 101)
        }
        if (android.os.Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 102)
        }
    }

    private fun startWakeService() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            status.text = "Microphone permission is required."
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 101)
            return
        }
        val i = Intent(this, WakeWordService::class.java)
        try {
            if (android.os.Build.VERSION.SDK_INT >= 26) startForegroundService(i) else startService(i)
            status.text = "Wake-word service requested. Check its notification."
        } catch (e: Exception) {
            status.text = "Could not start. Open the app and try again; Android may restrict background microphone use."
        }
    }
}
