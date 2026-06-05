package com.offshore.platform.service;

import com.offshore.platform.entity.EmployeeInfo;
import java.util.List;

/**
 * employee_info 基础Service。
 */
public interface EmployeeInfoService {
    int create(EmployeeInfo employeeInfo);

    int update(EmployeeInfo employeeInfo);

    EmployeeInfo getById(Long id);

    List<EmployeeInfo> listAll();

    int removeById(Long id);
}
