package com.offshore.platform.service;

import com.offshore.platform.entity.EmployeeCertificate;
import java.util.List;

/**
 * employee_certificate 基础Service。
 */
public interface EmployeeCertificateService {
    int create(EmployeeCertificate employeeCertificate);

    int update(EmployeeCertificate employeeCertificate);

    EmployeeCertificate getById(Long id);

    List<EmployeeCertificate> listAll();

    int removeById(Long id);
}
