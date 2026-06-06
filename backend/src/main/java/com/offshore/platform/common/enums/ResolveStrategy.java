package com.offshore.platform.common.enums;

/**
 * Conflict resolve strategy — shared by backend and PC frontend.
 *
 * Backend: sync_conflict.resolve_strategy
 */
public enum ResolveStrategy {

    KEEP_SERVER("KEEP_SERVER", "保留服务器版本"),
    KEEP_CLIENT("KEEP_CLIENT", "保留客户端版本"),
    MANUAL_MERGE("MANUAL_MERGE", "人工合并"),
    IGNORE_CLIENT("IGNORE_CLIENT", "忽略客户端变更");

    private final String code;
    private final String message;

    ResolveStrategy(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public static ResolveStrategy fromCode(String code) {
        for (ResolveStrategy strategy : values()) {
            if (strategy.code.equals(code)) {
                return strategy;
            }
        }
        return KEEP_SERVER;
    }
}
