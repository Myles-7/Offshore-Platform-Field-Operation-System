package com.offshore.platform.service;

import com.offshore.platform.dto.qualification.CertificateRequest;
import com.offshore.platform.dto.qualification.EmployeeRequest;
import com.offshore.platform.vo.qualification.CertificateVO;
import com.offshore.platform.vo.qualification.EmployeeVO;
import com.offshore.platform.vo.qualification.QualificationCheckVO;
import com.offshore.platform.vo.qualification.QualificationStatusVO;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

public interface QualificationService {
    List<EmployeeVO> listEmployees();
    EmployeeVO getEmployee(Long id);
    EmployeeVO createEmployee(EmployeeRequest request, HttpServletRequest servletRequest);
    EmployeeVO updateEmployee(Long id, EmployeeRequest request, HttpServletRequest servletRequest);
    void deleteEmployee(Long id, HttpServletRequest servletRequest);
    List<CertificateVO> listCertificates(Long employeeId);
    CertificateVO createCertificate(Long employeeId, CertificateRequest request, HttpServletRequest servletRequest);
    CertificateVO updateCertificate(Long certificateId, CertificateRequest request, HttpServletRequest servletRequest);
    void deleteCertificate(Long certificateId, HttpServletRequest servletRequest);
    List<CertificateVO> warningCertificates();
    List<QualificationCheckVO> qualificationCandidates(Long workOrderId);
    QualificationStatusVO myQualificationStatus();
    QualificationCheckVO mobileWorkOrderQualificationCheck(Long workOrderId);
}
