package com.offshore.platform.mapper;

import com.offshore.platform.entity.MaintenanceProcess;
import java.util.List;

public interface MaintenanceProcessMapper {
    int insert(MaintenanceProcess maintenanceProcess);
    int updateById(MaintenanceProcess maintenanceProcess);
    MaintenanceProcess selectById(Long id);
    List<MaintenanceProcess> selectAll();
    int softDeleteById(Long id);
}
