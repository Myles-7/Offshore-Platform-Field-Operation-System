package com.offshore.platform.common.enums;

/**
 * Sync module type — maps to both moduleType and entityType in sync protocol.
 *
 * Each value corresponds to one or more database tables.
 */
public enum SyncModuleType {

    /** 工单基础信息 — work_order */
    WORK_ORDER("WORK_ORDER", "工单信息", "work_order"),
    /** 施工记录 — work_order_record */
    WORK_RECORD("WORK_RECORD", "施工记录", "work_order_record"),
    /** 附件元数据 — work_order_attachment, file_storage */
    ATTACHMENT_META("ATTACHMENT_META", "附件元数据", "work_order_attachment"),
    /** 电子签名 — work_order_signature */
    SIGNATURE("SIGNATURE", "电子签名", "work_order_signature"),
    /** 验收记录 — work_order_acceptance */
    ACCEPTANCE("ACCEPTANCE", "验收记录", "work_order_acceptance"),
    /** PDF 验收单元数据 — work_order_pdf */
    PDF("PDF", "PDF验收单", "work_order_pdf"),
    /** 物料使用记录 — work_order_material_usage */
    MATERIAL_USAGE("MATERIAL_USAGE", "物料使用", "work_order_material_usage"),
    /** 人员资质状态 — employee_certificate, qualification_type */
    QUALIFICATION("QUALIFICATION", "人员资质", "employee_certificate"),
    /** AI 识别结果 — ai_result, ai_defect_box */
    AI_RESULT("AI_RESULT", "AI结果", "ai_result"),
    /** 知识库缓存 — knowledge_case */
    KNOWLEDGE("KNOWLEDGE", "知识库", "knowledge_case"),
    /** 设备注册与心跳 — device_info */
    DEVICE("DEVICE", "设备信息", "device_info"),
    /** 用户档案 — sys_user */
    USER_PROFILE("USER_PROFILE", "用户信息", "sys_user");

    private final String code;
    private final String message;
    private final String tableName;

    SyncModuleType(String code, String message, String tableName) {
        this.code = code;
        this.message = message;
        this.tableName = tableName;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    /**
     * Returns the primary database table name associated with this module type.
     */
    public String getTableName() {
        return tableName;
    }

    public static SyncModuleType fromCode(String code) {
        for (SyncModuleType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }

    public static SyncModuleType requireFromCode(String code) {
        SyncModuleType type = fromCode(code);
        if (type == null) {
            throw new IllegalArgumentException("Unknown SyncModuleType code: " + code);
        }
        return type;
    }
}
