package com.offshore.platform.dto.sync;

import jakarta.validation.constraints.NotBlank;

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
}
