package com.example.instagram.common

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager

class InternetBroadcastReceiver(val internetConn: (Boolean) -> Unit) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (ConnectivityManager.CONNECTIVITY_ACTION == intent?.action) {
            val noConnectivity = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false)
            internetConn(noConnectivity)
        }
    }
}