package com.offshore.platform.mapper;

import com.offshore.platform.entity.DeviceInfo;
import java.util.List;

/**
 * device_info 基础Mapper。
 */
public interface DeviceInfoMapper {
    int insert(DeviceInfo deviceInfo);

    int updateById(DeviceInfo deviceInfo);

    DeviceInfo selectById(Long id);

    List<DeviceInfo> selectAll();

    int softDeleteById(Long id);
}
