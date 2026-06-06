package com.offshore.platform.common.enums;

/**
 * Sync conflict type — shared by backend, mobile, and PC frontend.
 *
 * Backend: sync_conflict.conflict_type
 */
public enum ConflictType {

    VERSION_CONFLICT("VERSION_CONFLICT", "版本冲突"),
    FIELD_CONFLICT("FIELD_CONFLICT", "字段冲突"),
    UPDATE_AFTER_DELETE("UPDATE_AFTER_DELETE", "删除后更新冲突"),
    DELETE_AFTER_UPDATE("DELETE_AFTER_UPDATE", "更新后删除冲突"),
    DUPLICATE_CREATE("DUPLICATE_CREATE", "重复创建冲突"),
    PERMISSION_CONFLICT("PERMISSION_CONFLICT", "权限冲突"),
    FILE_META_CONFLICT("FILE_META_CONFLICT", "文件元数据冲突"),
    ACCEPTANCE_LOCKED_CONFLICT("ACCEPTANCE_LOCKED_CONFLICT", "验收锁定冲突");

    private final String code;
    private final String message;

    ConflictType(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public static ConflictType fromCode(String code) {
        for (ConflictType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return VERSION_CONFLICT;
    }
}
