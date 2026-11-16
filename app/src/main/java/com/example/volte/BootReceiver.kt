package com.example.volte

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Works only if Shizuku is already running; otherwise it silently does nothing.
        VolteApplier.apply(context)
    }
}
