package com.offshore.platform.service.impl;

import com.offshore.platform.entity.EmployeeInfo;
import com.offshore.platform.mapper.EmployeeInfoMapper;
import com.offshore.platform.service.EmployeeInfoService;
import java.util.List;

import org.springframework.stereotype.Service;

/**
 * employee_info 基础Service实现。
 */
@Service
public class EmployeeInfoServiceImpl implements EmployeeInfoService {
    private final EmployeeInfoMapper employeeInfoMapper;

    public EmployeeInfoServiceImpl(EmployeeInfoMapper employeeInfoMapper) {
        this.employeeInfoMapper = employeeInfoMapper;
    }

    @Override
    public int create(EmployeeInfo employeeInfo) {
        return employeeInfoMapper.insert(employeeInfo);
    }

    @Override
    public int update(EmployeeInfo employeeInfo) {
        return employeeInfoMapper.updateById(employeeInfo);
    }

    @Override
    public EmployeeInfo getById(Long id) {
        return employeeInfoMapper.selectById(id);
    }

    @Override
    public List<EmployeeInfo> listAll() {
        return employeeInfoMapper.selectAll();
    }

    @Override
    public int removeById(Long id) {
        return employeeInfoMapper.softDeleteById(id);
    }
}
