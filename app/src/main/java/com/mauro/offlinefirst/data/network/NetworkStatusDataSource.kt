package com.mauro.offlinefirst.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkStatusDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    val isConnected: Flow<Boolean> = callbackFlow @androidx.annotation.RequiresPermission(android.Manifest.permission.ACCESS_NETWORK_STATE) {

        val connectivityManager = context
            .getSystemService(Context.CONNECTIVITY_SERVICE)
                as ConnectivityManager

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(true)
            }
            override fun onLost(network: Network) {
                trySend(false)
            }
        }

        connectivityManager.registerDefaultNetworkCallback(callback)

        val isCurrentlyConnected = connectivityManager
            .activeNetwork != null
        trySend(isCurrentlyConnected)

        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }
}