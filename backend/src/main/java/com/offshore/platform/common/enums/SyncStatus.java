package com.offshore.platform.common.enums;

/**
 * Sync status — shared by backend, mobile, and PC frontend.
 *
 * All syncable entities carry this status.
 */
public enum SyncStatus {

    LOCAL_ONLY("LOCAL_ONLY", "仅本地"),
    PENDING("PENDING", "待同步"),
    SYNCING("SYNCING", "同步中"),
    SYNCED("SYNCED", "已同步"),
    FAILED("FAILED", "同步失败"),
    CONFLICT("CONFLICT", "冲突待复核"),
    DELETED("DELETED", "已删除"),
    IGNORED("IGNORED", "已忽略");

    private final String code;
    private final String message;

    SyncStatus(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public static SyncStatus fromCode(String code) {
        for (SyncStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return PENDING;
    }
}
