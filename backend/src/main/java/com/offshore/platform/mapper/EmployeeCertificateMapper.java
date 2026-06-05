package com.offshore.platform.mapper;

import com.offshore.platform.entity.EmployeeCertificate;
import java.util.List;

/**
 * employee_certificate 基础Mapper。
 */
public interface EmployeeCertificateMapper {
    int insert(EmployeeCertificate employeeCertificate);

    int updateById(EmployeeCertificate employeeCertificate);

    EmployeeCertificate selectById(Long id);

    List<EmployeeCertificate> selectAll();

    int softDeleteById(Long id);
}
