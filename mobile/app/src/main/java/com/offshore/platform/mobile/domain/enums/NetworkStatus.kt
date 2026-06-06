package com.offshore.platform.mobile.domain.enums

/**
 * Current network connectivity state.
 * Used by NetworkMonitor and UI composables.
 */
enum class NetworkStatus(val displayName: String) {
    CONNECTED("已连接"),
    DISCONNECTED("离线"),
    METERED("按流量计费"),
    UNKNOWN("未知");
}
