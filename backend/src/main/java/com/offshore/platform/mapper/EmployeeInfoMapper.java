package com.offshore.platform.mapper;

import com.offshore.platform.entity.EmployeeInfo;
import java.util.List;

/**
 * employee_info 基础Mapper。
 */
public interface EmployeeInfoMapper {
    int insert(EmployeeInfo employeeInfo);

    int updateById(EmployeeInfo employeeInfo);

    EmployeeInfo selectById(Long id);

    List<EmployeeInfo> selectAll();

    int softDeleteById(Long id);
}
