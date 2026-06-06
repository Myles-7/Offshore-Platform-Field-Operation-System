package com.offshore.platform.common.enums;

/**
 * Sync action type — CREATE, UPDATE, or DELETE.
 *
 * Aligned with mobile SyncActionType and PC frontend.
 */
public enum SyncActionType {

    CREATE("CREATE", "新增"),
    UPDATE("UPDATE", "更新"),
    DELETE("DELETE", "删除");

    private final String code;
    private final String message;

    SyncActionType(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public static SyncActionType fromCode(String code) {
        for (SyncActionType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return CREATE;
    }
}
