package com.kazakago.swr.compose.internal

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

internal class NetworkCallbackManager private constructor(
    private val context: Context,
) {

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var instance: NetworkCallbackManager? = null

        fun getInstance(context: Context): NetworkCallbackManager {
            var instance = instance
            return if (instance != null && instance.context == context.applicationContext) {
                instance
            } else {
                instance = NetworkCallbackManager(context.applicationContext)
                this.instance = instance
                instance
            }
        }
    }

    private val _onAvailable = MutableSharedFlow<Network>(extraBufferCapacity = 1)
    val onAvailable = _onAvailable.asSharedFlow()

    init {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            .build()
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                _onAvailable.tryEmit(network)
            }
        }
        connectivityManager.registerNetworkCallback(request, networkCallback)
    }
}
