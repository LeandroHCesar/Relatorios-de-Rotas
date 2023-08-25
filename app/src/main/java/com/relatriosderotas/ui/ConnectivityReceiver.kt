package com.relatriosderotas.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo

class ConnectivityReceiver(private val listener: OnConnectivityChangeListener) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null) {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetworkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
            val isConnected = activeNetworkInfo != null && activeNetworkInfo.isConnected

            listener.onConnectivityChange(isConnected)
        }
    }

    interface OnConnectivityChangeListener {
        fun onConnectivityChange(isConnected: Boolean)
    }
}
