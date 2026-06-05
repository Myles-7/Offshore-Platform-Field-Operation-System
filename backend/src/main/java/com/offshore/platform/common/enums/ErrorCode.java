package com.offshore.platform.common.enums;

/**
 * Unified API error codes.
 */
public enum ErrorCode {
    SUCCESS(200, "success"),
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "无访问权限"),
    NOT_FOUND(404, "资源不存在"),
    USER_ERROR(10001, "用户错误"),
    WORK_ORDER_ERROR(20001, "工单错误"),
    FILE_ERROR(30001, "文件错误"),
    SYNC_ERROR(40001, "同步错误"),
    AI_ERROR(50001, "AI错误"),
    PDF_ERROR(50002, "PDF错误"),
    MATERIAL_ERROR(60001, "物料错误"),
    QUALIFICATION_ERROR(70001, "资质错误"),
    DASHBOARD_ERROR(80001, "看板错误"),
    SYSTEM_ERROR(500, "系统错误");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
