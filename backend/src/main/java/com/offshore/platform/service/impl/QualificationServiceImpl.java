package com.offshore.platform.service.impl;

import com.offshore.platform.common.context.CurrentUser;
import com.offshore.platform.common.context.CurrentUserContext;
import com.offshore.platform.common.enums.ErrorCode;
import com.offshore.platform.common.exception.BusinessException;
import com.offshore.platform.common.util.TraceIdUtils;
import com.offshore.platform.dto.qualification.CertificateRequest;
import com.offshore.platform.dto.qualification.EmployeeRequest;
import com.offshore.platform.entity.EmployeeCertificate;
import com.offshore.platform.entity.EmployeeInfo;
import com.offshore.platform.entity.OperationLog;
import com.offshore.platform.entity.QualificationType;
import com.offshore.platform.entity.WorkOrder;
import com.offshore.platform.entity.WorkOrderQualificationCheck;
import com.offshore.platform.mapper.EmployeeCertificateMapper;
import com.offshore.platform.mapper.EmployeeInfoMapper;
import com.offshore.platform.mapper.OperationLogMapper;
import com.offshore.platform.mapper.QualificationTypeMapper;
import com.offshore.platform.mapper.WorkOrderAssignmentMapper;
import com.offshore.platform.mapper.WorkOrderMapper;
import com.offshore.platform.mapper.WorkOrderQualificationCheckMapper;
import com.offshore.platform.service.DataScopeService;
import com.offshore.platform.service.QualificationService;
import com.offshore.platform.vo.qualification.CertificateVO;
import com.offshore.platform.vo.qualification.EmployeeVO;
import com.offshore.platform.vo.qualification.QualificationCheckVO;
import com.offshore.platform.vo.qualification.QualificationStatusVO;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QualificationServiceImpl implements QualificationService {
    private static final int EXPIRING_WARNING_DAYS = 30;

    private final EmployeeInfoMapper employeeMapper;
    private final EmployeeCertificateMapper certificateMapper;
    private final QualificationTypeMapper typeMapper;
    private final WorkOrderMapper workOrderMapper;
    private final WorkOrderAssignmentMapper assignmentMapper;
    private final WorkOrderQualificationCheckMapper checkMapper;
    private final OperationLogMapper operationLogMapper;
    private final DataScopeService dataScopeService;

    public QualificationServiceImpl(EmployeeInfoMapper employeeMapper, EmployeeCertificateMapper certificateMapper,
            QualificationTypeMapper typeMapper, WorkOrderMapper workOrderMapper, WorkOrderAssignmentMapper assignmentMapper,
            WorkOrderQualificationCheckMapper checkMapper, OperationLogMapper operationLogMapper,
            DataScopeService dataScopeService) {
        this.employeeMapper = employeeMapper;
        this.certificateMapper = certificateMapper;
        this.typeMapper = typeMapper;
        this.workOrderMapper = workOrderMapper;
        this.assignmentMapper = assignmentMapper;
        this.checkMapper = checkMapper;
        this.operationLogMapper = operationLogMapper;
        this.dataScopeService = dataScopeService;
    }

    @Override
    public List<EmployeeVO> listEmployees() {
        requireQualificationAdmin(CurrentUserContext.require());
        return employeeMapper.selectAll().stream().map(this::toEmployeeVO).toList();
    }

    @Override
    public EmployeeVO getEmployee(Long id) {
        requireQualificationAdmin(CurrentUserContext.require());
        return toEmployeeVO(requireEmployee(id));
    }

    @Override
    @Transactional
    public EmployeeVO createEmployee(EmployeeRequest request, HttpServletRequest servletRequest) {
        CurrentUser user = CurrentUserContext.require();
        requireQualificationAdmin(user);
        EmployeeInfo employee = new EmployeeInfo();
        fillEmployee(employee, request, user, true);
        employeeMapper.insert(employee);
        writeOperationLog(user, servletRequest, "CREATE_EMPLOYEE", "EMPLOYEE", employee.getId(), employee.getEmployeeNo());
        return toEmployeeVO(employee);
    }

    @Override
    @Transactional
    public EmployeeVO updateEmployee(Long id, EmployeeRequest request, HttpServletRequest servletRequest) {
        CurrentUser user = CurrentUserContext.require();
        requireQualificationAdmin(user);
        EmployeeInfo employee = requireEmployee(id);
        fillEmployee(employee, request, user, false);
        employeeMapper.updateById(employee);
        writeOperationLog(user, servletRequest, "UPDATE_EMPLOYEE", "EMPLOYEE", employee.getId(), employee.getEmployeeNo());
        return toEmployeeVO(employee);
    }

    @Override
    @Transactional
    public void deleteEmployee(Long id, HttpServletRequest servletRequest) {
        CurrentUser user = CurrentUserContext.require();
        requireQualificationAdmin(user);
        EmployeeInfo employee = requireEmployee(id);
        employeeMapper.softDeleteById(id);
        writeOperationLog(user, servletRequest, "DELETE_EMPLOYEE", "EMPLOYEE", id, employee.getEmployeeNo());
    }

    @Override
    public List<CertificateVO> listCertificates(Long employeeId) {
        requireQualificationAdmin(CurrentUserContext.require());
        requireEmployee(employeeId);
        return employeeCertificates(employeeId).stream().map(this::toCertificateVO).toList();
    }

    @Override
    @Transactional
    public CertificateVO createCertificate(Long employeeId, CertificateRequest request, HttpServletRequest servletRequest) {
        CurrentUser user = CurrentUserContext.require();
        requireQualificationAdmin(user);
        requireEmployee(employeeId);
        EmployeeCertificate certificate = new EmployeeCertificate();
        fillCertificate(certificate, employeeId, request, user, true);
        certificateMapper.insert(certificate);
        writeOperationLog(user, servletRequest, "CREATE_CERTIFICATE", "EMPLOYEE_CERTIFICATE", certificate.getId(),
                certificate.getCertificateNo());
        return toCertificateVO(certificate);
    }

    @Override
    @Transactional
    public CertificateVO updateCertificate(Long certificateId, CertificateRequest request, HttpServletRequest servletRequest) {
        CurrentUser user = CurrentUserContext.require();
        requireQualificationAdmin(user);
        EmployeeCertificate certificate = requireCertificate(certificateId);
        fillCertificate(certificate, certificate.getEmployeeId(), request, user, false);
        certificateMapper.updateById(certificate);
        writeOperationLog(user, servletRequest, "UPDATE_CERTIFICATE", "EMPLOYEE_CERTIFICATE", certificate.getId(),
                certificate.getCertificateNo());
        return toCertificateVO(certificate);
    }

    @Override
    @Transactional
    public void deleteCertificate(Long certificateId, HttpServletRequest servletRequest) {
        CurrentUser user = CurrentUserContext.require();
        requireQualificationAdmin(user);
        EmployeeCertificate certificate = requireCertificate(certificateId);
        certificateMapper.softDeleteById(certificateId);
        writeOperationLog(user, servletRequest, "DELETE_CERTIFICATE", "EMPLOYEE_CERTIFICATE", certificateId,
                certificate.getCertificateNo());
    }

    @Override
    public List<CertificateVO> warningCertificates() {
        requireQualificationAdmin(CurrentUserContext.require());
        return certificateMapper.selectAll().stream()
                .filter(certificate -> !"VALID".equals(certificateStatus(certificate)))
                .map(certificate -> {
                    certificate.setValidStatus(certificateStatus(certificate));
                    certificate.setWarningLevel("EXPIRED".equals(certificate.getValidStatus()) ? "HIGH" : "MEDIUM");
                    return toCertificateVO(certificate);
                }).toList();
    }

    @Override
    public List<QualificationCheckVO> qualificationCandidates(Long workOrderId) {
        CurrentUser user = CurrentUserContext.require();
        WorkOrder workOrder = requireAdminWorkOrder(workOrderId, user);
        return employeeMapper.selectAll().stream()
                .filter(employee -> "ACTIVE".equals(employee.getEmployeeStatus()))
                .map(employee -> checkQualification(workOrder, employee, user, true))
                .toList();
    }

    @Override
    public QualificationStatusVO myQualificationStatus() {
        CurrentUser user = CurrentUserContext.require();
        EmployeeInfo employee = currentEmployee(user);
        QualificationStatusVO vo = new QualificationStatusVO();
        vo.employeeId = employee.getId();
        vo.employeeName = employee.getRealName();
        vo.certificates = employeeCertificates(employee.getId()).stream().map(certificate -> {
            certificate.setValidStatus(certificateStatus(certificate));
            return toCertificateVO(certificate);
        }).toList();
        vo.overallStatus = vo.certificates.stream().anyMatch(c -> "EXPIRED".equals(c.validStatus) || "REVOKED".equals(c.validStatus))
                ? "FAILED"
                : vo.certificates.stream().anyMatch(c -> "EXPIRING".equals(c.validStatus)) ? "EXPIRING" : "PASSED";
        return vo;
    }

    @Override
    public QualificationCheckVO mobileWorkOrderQualificationCheck(Long workOrderId) {
        CurrentUser user = CurrentUserContext.require();
        WorkOrder workOrder = requireMobileWorkOrder(workOrderId, user);
        return checkQualification(workOrder, currentEmployee(user), user, true);
    }

    private QualificationCheckVO checkQualification(WorkOrder workOrder, EmployeeInfo employee, CurrentUser user,
            boolean writeCheck) {
        List<QualificationType> requiredTypes = typeMapper.selectAll().stream()
                .filter(type -> Integer.valueOf(1).equals(type.getEnabledFlag()) && Integer.valueOf(1).equals(type.getRequiredFlag()))
                .toList();
        List<EmployeeCertificate> certificates = employeeCertificates(employee.getId());

        String result = "PASSED";
        String message = "Qualification passed";
        Long certificateId = null;
        Long qualificationTypeId = null;

        for (QualificationType type : requiredTypes) {
            qualificationTypeId = type.getId();
            EmployeeCertificate certificate = certificates.stream()
                    .filter(c -> type.getId().equals(c.getQualificationTypeId()))
                    .findFirst()
                    .orElse(null);
            if (certificate == null) {
                result = "FAILED";
                message = "Missing required qualification: " + type.getQualificationName();
                break;
            }
            certificateId = certificate.getId();
            String status = certificateStatus(certificate);
            if ("EXPIRED".equals(status) || "REVOKED".equals(status)) {
                result = "FAILED";
                message = "Certificate unavailable: " + certificate.getCertificateName();
                break;
            }
            if ("EXPIRING".equals(status)) {
                result = "EXPIRING";
                message = "Certificate expiring soon: " + certificate.getCertificateName();
            }
        }

        WorkOrderQualificationCheck check = new WorkOrderQualificationCheck();
        check.setWorkOrderId(workOrder.getId());
        check.setEmployeeId(employee.getId());
        check.setCertificateId(certificateId);
        check.setQualificationTypeId(qualificationTypeId);
        check.setCheckResult(result);
        check.setCheckTime(LocalDateTime.now());
        check.setCheckerId(user.getUserId());
        check.setVersion(1);
        check.setSyncStatus("SYNCED");
        check.setOperatorId(user.getUserId());
        check.setCreatedAt(LocalDateTime.now());
        check.setUpdatedAt(LocalDateTime.now());
        check.setDeletedFlag(0);
        check.setCreatedBy(user.getUserId());
        check.setUpdatedBy(user.getUserId());
        check.setRemark(message);
        if (writeCheck) {
            checkMapper.insert(check);
            check.setServerId(check.getId());
            checkMapper.updateById(check);
        }

        QualificationCheckVO vo = new QualificationCheckVO();
        vo.id = check.getId();
        vo.workOrderId = workOrder.getId();
        vo.employeeId = employee.getId();
        vo.certificateId = certificateId;
        vo.qualificationTypeId = qualificationTypeId;
        vo.checkResult = result;
        vo.message = message;
        return vo;
    }

    private String certificateStatus(EmployeeCertificate certificate) {
        if ("REVOKED".equals(certificate.getValidStatus())) {
            return "REVOKED";
        }
        if (certificate.getValidTo() != null && certificate.getValidTo().isBefore(LocalDate.now())) {
            return "EXPIRED";
        }
        if (certificate.getValidTo() != null && !certificate.getValidTo().isAfter(LocalDate.now().plusDays(EXPIRING_WARNING_DAYS))) {
            return "EXPIRING";
        }
        return certificate.getValidStatus() == null ? "VALID" : certificate.getValidStatus();
    }

    private EmployeeInfo currentEmployee(CurrentUser user) {
        return employeeMapper.selectAll().stream()
                .filter(employee -> user.getUserId().equals(employee.getUserId()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Employee profile not found"));
    }

    private List<EmployeeCertificate> employeeCertificates(Long employeeId) {
        return certificateMapper.selectAll().stream()
                .filter(certificate -> employeeId.equals(certificate.getEmployeeId()))
                .toList();
    }

    private EmployeeInfo requireEmployee(Long id) {
        EmployeeInfo employee = employeeMapper.selectById(id);
        if (employee == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Employee not found");
        }
        return employee;
    }

    private EmployeeCertificate requireCertificate(Long id) {
        EmployeeCertificate certificate = certificateMapper.selectById(id);
        if (certificate == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Certificate not found");
        }
        return certificate;
    }

    private void requireQualificationAdmin(CurrentUser user) {
        if (!dataScopeService.canAccessAll(user) && !user.getRoleCodes().contains("QUALIFICATION_MANAGER")
                && !"QUALIFICATION".equals(user.getDataScope())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "No permission for qualification management");
        }
    }

    private WorkOrder requireAdminWorkOrder(Long id, CurrentUser user) {
        WorkOrder workOrder = requireWorkOrder(id);
        if (!dataScopeService.canAccessAll(user) && !dataScopeService.canAccessProject(user, workOrder.getProjectId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "No permission for work order");
        }
        return workOrder;
    }

    private WorkOrder requireMobileWorkOrder(Long id, CurrentUser user) {
        WorkOrder workOrder = requireWorkOrder(id);
        boolean assigned = assignmentMapper.selectAll().stream()
                .anyMatch(assignment -> id.equals(assignment.getWorkOrderId()) && user.getUserId().equals(assignment.getAssigneeId()));
        if (!user.getUserId().equals(workOrder.getMaintainerId()) && !assigned) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "No permission for work order");
        }
        return workOrder;
    }

    private WorkOrder requireWorkOrder(Long id) {
        WorkOrder workOrder = workOrderMapper.selectById(id);
        if (workOrder == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Work order not found");
        }
        return workOrder;
    }

    private void fillEmployee(EmployeeInfo employee, EmployeeRequest request, CurrentUser user, boolean create) {
        employee.setUserId(request.userId);
        employee.setEmployeeNo(request.employeeNo);
        employee.setRealName(request.realName);
        employee.setPhone(request.phone);
        employee.setIdCardHash(request.idCardHash);
        employee.setDepartmentId(request.departmentId);
        employee.setPositionName(request.positionName);
        employee.setEmployeeStatus(request.employeeStatus == null ? "ACTIVE" : request.employeeStatus);
        employee.setUpdatedAt(LocalDateTime.now());
        employee.setUpdatedBy(user.getUserId());
        employee.setRemark(request.remark);
        if (create) {
            employee.setCreatedAt(LocalDateTime.now());
            employee.setDeletedFlag(0);
            employee.setCreatedBy(user.getUserId());
        }
    }

    private void fillCertificate(EmployeeCertificate certificate, Long employeeId, CertificateRequest request, CurrentUser user,
            boolean create) {
        certificate.setEmployeeId(employeeId);
        certificate.setQualificationTypeId(request.qualificationTypeId);
        certificate.setCertificateNo(request.certificateNo);
        certificate.setCertificateName(request.certificateName);
        certificate.setIssueOrg(request.issueOrg);
        certificate.setIssueDate(request.issueDate);
        certificate.setValidFrom(request.validFrom);
        certificate.setValidTo(request.validTo);
        certificate.setValidStatus(request.validStatus == null ? "VALID" : request.validStatus);
        certificate.setWarningLevel(request.warningLevel);
        certificate.setFileId(request.fileId);
        certificate.setUpdatedAt(LocalDateTime.now());
        certificate.setUpdatedBy(user.getUserId());
        certificate.setRemark(request.remark);
        if (create) {
            certificate.setCreatedAt(LocalDateTime.now());
            certificate.setDeletedFlag(0);
            certificate.setCreatedBy(user.getUserId());
        }
    }

    private EmployeeVO toEmployeeVO(EmployeeInfo employee) {
        EmployeeVO vo = new EmployeeVO();
        vo.id = employee.getId();
        vo.userId = employee.getUserId();
        vo.employeeNo = employee.getEmployeeNo();
        vo.realName = employee.getRealName();
        vo.phone = employee.getPhone();
        vo.positionName = employee.getPositionName();
        vo.employeeStatus = employee.getEmployeeStatus();
        return vo;
    }

    private CertificateVO toCertificateVO(EmployeeCertificate certificate) {
        CertificateVO vo = new CertificateVO();
        vo.id = certificate.getId();
        vo.employeeId = certificate.getEmployeeId();
        vo.qualificationTypeId = certificate.getQualificationTypeId();
        vo.certificateNo = certificate.getCertificateNo();
        vo.certificateName = certificate.getCertificateName();
        vo.validTo = certificate.getValidTo();
        vo.validStatus = certificate.getValidStatus();
        vo.warningLevel = certificate.getWarningLevel();
        vo.fileId = certificate.getFileId();
        return vo;
    }

    private void writeOperationLog(CurrentUser user, HttpServletRequest request, String operationType, String businessType,
            Long businessId, String businessNo) {
        OperationLog log = new OperationLog();
        log.setTraceId(TraceIdUtils.currentTraceId());
        log.setOperatorId(user.getUserId());
        log.setOperatorName(user.getRealName());
        log.setRoleCode(String.join(",", user.getRoleCodes()));
        log.setPlatform("PC");
        log.setModuleName("QUALIFICATION");
        log.setOperationType(operationType);
        log.setBusinessType(businessType);
        log.setBusinessId(String.valueOf(businessId));
        log.setBusinessNo(businessNo);
        log.setRequestMethod(request.getMethod());
        log.setRequestPath(request.getRequestURI());
        log.setRequestIp(request.getRemoteAddr());
        log.setUserAgent(request.getHeader("User-Agent"));
        log.setResultStatus("SUCCESS");
        log.setOperationTime(LocalDateTime.now());
        log.setDeletedFlag(0);
        operationLogMapper.insert(log);
    }
}
