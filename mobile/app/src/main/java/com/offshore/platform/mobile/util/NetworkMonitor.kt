package com.offshore.platform.mobile.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.offshore.platform.mobile.domain.enums.NetworkStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Monitors network connectivity and exposes a [StateFlow] of [NetworkStatus].
 *
 * Usage:
 *  val status by networkMonitor.status.collectAsStateWithLifecycle()
 *  if (status == NetworkStatus.DISCONNECTED) showOfflineBanner()
 */
@Singleton
class NetworkMonitor @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val _status = MutableStateFlow(NetworkStatus.UNKNOWN)
    val status: StateFlow<NetworkStatus> = _status.asStateFlow()

    private val connectivityManager: ConnectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            _status.value = evaluateStatus()
        }

        override fun onLost(network: Network) {
            _status.value = NetworkStatus.DISCONNECTED
        }

        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            _status.value = evaluateStatus()
        }
    }

    init {
        // Start observing
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, networkCallback)

        // Initial value
        _status.value = evaluateStatus()
    }

    private fun evaluateStatus(): NetworkStatus {
        val activeNetwork = connectivityManager.activeNetwork ?: return NetworkStatus.DISCONNECTED
        val caps = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return NetworkStatus.DISCONNECTED

        return when {
            !caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ->
                NetworkStatus.DISCONNECTED
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED) ->
                NetworkStatus.CONNECTED
            else ->
                NetworkStatus.METERED
        }
    }

    val isOnline: Boolean
        get() = _status.value == NetworkStatus.CONNECTED || _status.value == NetworkStatus.METERED
}
