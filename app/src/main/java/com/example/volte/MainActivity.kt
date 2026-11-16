package com.example.volte

import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import rikka.shizuku.Shizuku

class MainActivity : Activity() {
    private lateinit var status: TextView

    private val permListener = Shizuku.OnRequestPermissionResultListener { _, _ -> runApply() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pad = (24 * resources.displayMetrics.density).toInt()
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad, pad, pad)
        }
        status = TextView(this).apply { textSize = 16f; text = "Ready." }
        val btn = Button(this).apply {
            text = "Enable VoLTE"
            setOnClickListener { start() }
        }
        root.addView(btn)
        root.addView(status)
        setContentView(root)
        Shizuku.addRequestPermissionResultListener(permListener)
        requestPermissions(arrayOf(android.Manifest.permission.READ_PHONE_STATE), 1)
    }

    override fun onDestroy() {
        Shizuku.removeRequestPermissionResultListener(permListener)
        super.onDestroy()
    }

    private fun start() {
        if (!Shizuku.pingBinder()) { status.text = "Shizuku is not running. Start it first."; return }
        if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
            Shizuku.requestPermission(0)
        } else runApply()
    }

    private fun runApply() {
        Thread {
            val r = VolteApplier.apply(this)
            runOnUiThread { status.text = r }
        }.start()
    }
}
