package com.offshore.platform.service.impl;

import com.offshore.platform.entity.EmployeeCertificate;
import com.offshore.platform.mapper.EmployeeCertificateMapper;
import com.offshore.platform.service.EmployeeCertificateService;
import java.util.List;

import org.springframework.stereotype.Service;

/**
 * employee_certificate 基础Service实现。
 */
@Service
public class EmployeeCertificateServiceImpl implements EmployeeCertificateService {
    private final EmployeeCertificateMapper employeeCertificateMapper;

    public EmployeeCertificateServiceImpl(EmployeeCertificateMapper employeeCertificateMapper) {
        this.employeeCertificateMapper = employeeCertificateMapper;
    }

    @Override
    public int create(EmployeeCertificate employeeCertificate) {
        return employeeCertificateMapper.insert(employeeCertificate);
    }

    @Override
    public int update(EmployeeCertificate employeeCertificate) {
        return employeeCertificateMapper.updateById(employeeCertificate);
    }

    @Override
    public EmployeeCertificate getById(Long id) {
        return employeeCertificateMapper.selectById(id);
    }

    @Override
    public List<EmployeeCertificate> listAll() {
        return employeeCertificateMapper.selectAll();
    }

    @Override
    public int removeById(Long id) {
        return employeeCertificateMapper.softDeleteById(id);
    }
}
