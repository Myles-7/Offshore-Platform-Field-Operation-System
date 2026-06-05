package com.offshore.platform.service.impl;

import com.offshore.platform.entity.DeviceInfo;
import com.offshore.platform.mapper.DeviceInfoMapper;
import com.offshore.platform.service.DeviceInfoService;
import java.util.List;

import org.springframework.stereotype.Service;

/**
 * device_info 基础Service实现。
 */
@Service
public class DeviceInfoServiceImpl implements DeviceInfoService {
    private final DeviceInfoMapper deviceInfoMapper;

    public DeviceInfoServiceImpl(DeviceInfoMapper deviceInfoMapper) {
        this.deviceInfoMapper = deviceInfoMapper;
    }

    @Override
    public int create(DeviceInfo deviceInfo) {
        return deviceInfoMapper.insert(deviceInfo);
    }

    @Override
    public int update(DeviceInfo deviceInfo) {
        return deviceInfoMapper.updateById(deviceInfo);
    }

    @Override
    public DeviceInfo getById(Long id) {
        return deviceInfoMapper.selectById(id);
    }

    @Override
    public List<DeviceInfo> listAll() {
        return deviceInfoMapper.selectAll();
    }

    @Override
    public int removeById(Long id) {
        return deviceInfoMapper.softDeleteById(id);
    }
}
