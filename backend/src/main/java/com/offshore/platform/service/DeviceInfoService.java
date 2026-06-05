package com.offshore.platform.service;

import com.offshore.platform.entity.DeviceInfo;
import java.util.List;

/**
 * device_info 基础Service。
 */
public interface DeviceInfoService {
    int create(DeviceInfo deviceInfo);

    int update(DeviceInfo deviceInfo);

    DeviceInfo getById(Long id);

    List<DeviceInfo> listAll();

    int removeById(Long id);
}
