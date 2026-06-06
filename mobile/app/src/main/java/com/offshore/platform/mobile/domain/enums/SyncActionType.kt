package com.offshore.platform.mobile.domain.enums

/**
 * Action type for sync push items — CREATE, UPDATE, or DELETE.
 *
 * Backend: SyncPushItem.actionType
 */
enum class SyncActionType(val code: String) {
    CREATE("CREATE"),
    UPDATE("UPDATE"),
    DELETE("DELETE");

    companion object {
        fun fromCode(code: String): SyncActionType =
            entries.find { it.code == code } ?: CREATE
    }
}
