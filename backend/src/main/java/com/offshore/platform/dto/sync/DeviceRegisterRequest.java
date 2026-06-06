package com.offshore.platform.dto.sync;

import jakarta.validation.constraints.NotBlank;

/**
 * Device register and heartbeat request.
 *
 * POST /api/sync/device/register — first-time registration
 * POST /api/sync/device/heartbeat — periodic heartbeat (reuses same DTO)
 */
public class DeviceRegisterRequest {
    @NotBlank(message = "deviceId不能为空")
    public String deviceId;
    public String deviceName;
    public String platform;
    public String osVersion;
    public String appVersion;
    public String manufacturer;
    public String model;
    public String imeiHash;
    public String pushToken;
    /** 操作人 ID（心跳时由后端从 token 解析，可空） */
    public Long operatorId;
}
