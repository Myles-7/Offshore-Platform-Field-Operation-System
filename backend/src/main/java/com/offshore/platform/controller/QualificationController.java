package com.offshore.platform.controller;

import com.offshore.platform.common.response.ApiResponse;
import com.offshore.platform.common.log.OperationLog;
import com.offshore.platform.dto.qualification.CertificateRequest;
import com.offshore.platform.dto.qualification.EmployeeRequest;
import com.offshore.platform.service.QualificationService;
import com.offshore.platform.vo.qualification.CertificateVO;
import com.offshore.platform.vo.qualification.EmployeeVO;
import com.offshore.platform.vo.qualification.QualificationCheckVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@Tag(name = "qualification", description = "人员资质")
@RestController
public class QualificationController {
    private final QualificationService service;
    public QualificationController(QualificationService service){this.service=service;}
    @GetMapping("/api/admin/employees") public ApiResponse<List<EmployeeVO>> employees(){return ApiResponse.success(service.listEmployees());}
    @GetMapping("/api/admin/employees/{id}") public ApiResponse<EmployeeVO> employee(@PathVariable Long id){return ApiResponse.success(service.getEmployee(id));}
    @PostMapping("/api/admin/employees") public ApiResponse<EmployeeVO> createEmployee(@Valid @RequestBody EmployeeRequest r,HttpServletRequest req){return ApiResponse.success(service.createEmployee(r,req));}
    @PutMapping("/api/admin/employees/{id}") public ApiResponse<EmployeeVO> updateEmployee(@PathVariable Long id,@Valid @RequestBody EmployeeRequest r,HttpServletRequest req){return ApiResponse.success(service.updateEmployee(id,r,req));}
    @DeleteMapping("/api/admin/employees/{id}") public ApiResponse<Void> deleteEmployee(@PathVariable Long id,HttpServletRequest req){service.deleteEmployee(id,req);return ApiResponse.success();}
    @GetMapping("/api/admin/employees/{id}/certificates") public ApiResponse<List<CertificateVO>> certs(@PathVariable Long id){return ApiResponse.success(service.listCertificates(id));}
    @OperationLog(module = "QUALIFICATION", operation = "CREATE_CERTIFICATE", businessType = "EMPLOYEE_CERTIFICATE")
    @PostMapping("/api/admin/employees/{id}/certificates") public ApiResponse<CertificateVO> createCert(@PathVariable Long id,@Valid @RequestBody CertificateRequest r,HttpServletRequest req){return ApiResponse.success(service.createCertificate(id,r,req));}
    @OperationLog(module = "QUALIFICATION", operation = "UPDATE_CERTIFICATE", businessType = "EMPLOYEE_CERTIFICATE")
    @PutMapping("/api/admin/certificates/{id}") public ApiResponse<CertificateVO> updateCert(@PathVariable("id") Long id,@Valid @RequestBody CertificateRequest r,HttpServletRequest req){return ApiResponse.success(service.updateCertificate(id,r,req));}
    @OperationLog(module = "QUALIFICATION", operation = "DELETE_CERTIFICATE", businessType = "EMPLOYEE_CERTIFICATE")
    @DeleteMapping("/api/admin/certificates/{id}") public ApiResponse<Void> deleteCert(@PathVariable("id") Long id,HttpServletRequest req){service.deleteCertificate(id,req);return ApiResponse.success();}
    @GetMapping("/api/admin/certificates/warnings") public ApiResponse<List<CertificateVO>> warnings(){return ApiResponse.success(service.warningCertificates());}
    @GetMapping("/api/admin/work-orders/{id}/qualification-candidates") public ApiResponse<List<QualificationCheckVO>> candidates(@PathVariable("id") Long id){return ApiResponse.success(service.qualificationCandidates(id));}
    @GetMapping("/api/mobile/my/qualification-status") public ApiResponse<com.offshore.platform.vo.qualification.QualificationStatusVO> myStatus(){return ApiResponse.success(service.myQualificationStatus());}
}
