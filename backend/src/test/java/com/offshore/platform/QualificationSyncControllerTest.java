package com.offshore.platform;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.offshore.platform.dto.auth.LoginRequest;
import com.offshore.platform.entity.DeviceInfo;
import com.offshore.platform.entity.EmployeeInfo;
import com.offshore.platform.entity.OperationLog;
import com.offshore.platform.entity.ProjectInfo;
import com.offshore.platform.entity.QualificationType;
import com.offshore.platform.entity.SysRole;
import com.offshore.platform.entity.SysUser;
import com.offshore.platform.entity.SysUserRole;
import com.offshore.platform.entity.WorkOrder;
import com.offshore.platform.entity.WorkOrderAssignment;
import com.offshore.platform.mapper.DeviceInfoMapper;
import com.offshore.platform.mapper.EmployeeCertificateMapper;
import com.offshore.platform.mapper.EmployeeInfoMapper;
import com.offshore.platform.mapper.OperationLogMapper;
import com.offshore.platform.mapper.ProjectInfoMapper;
import com.offshore.platform.mapper.QualificationTypeMapper;
import com.offshore.platform.mapper.SyncConflictMapper;
import com.offshore.platform.mapper.SyncLogMapper;
import com.offshore.platform.mapper.SyncTaskMapper;
import com.offshore.platform.mapper.SysRoleMapper;
import com.offshore.platform.mapper.SysUserMapper;
import com.offshore.platform.mapper.SysUserRoleMapper;
import com.offshore.platform.mapper.WorkOrderAssignmentMapper;
import com.offshore.platform.mapper.WorkOrderAttachmentMapper;
import com.offshore.platform.mapper.WorkOrderMapper;
import com.offshore.platform.mapper.WorkOrderQualificationCheckMapper;
import com.offshore.platform.mapper.WorkOrderRecordMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = "spring.flyway.enabled=false")
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Sql(scripts = "/test-schema.sql")
@Transactional
class QualificationSyncControllerTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private SysUserMapper sysUserMapper;
    @Autowired private SysRoleMapper sysRoleMapper;
    @Autowired private SysUserRoleMapper sysUserRoleMapper;
    @Autowired private ProjectInfoMapper projectInfoMapper;
    @Autowired private WorkOrderMapper workOrderMapper;
    @Autowired private WorkOrderAssignmentMapper assignmentMapper;
    @Autowired private EmployeeInfoMapper employeeMapper;
    @Autowired private OperationLogMapper operationLogMapper;
    @Autowired private EmployeeCertificateMapper certificateMapper;
    @Autowired private QualificationTypeMapper qualificationTypeMapper;
    @Autowired private WorkOrderQualificationCheckMapper qualificationCheckMapper;
    @Autowired private DeviceInfoMapper deviceMapper;
    @Autowired private SyncTaskMapper syncTaskMapper;
    @Autowired private SyncLogMapper syncLogMapper;
    @Autowired private SyncConflictMapper syncConflictMapper;
    @Autowired private WorkOrderRecordMapper recordMapper;
    @Autowired private WorkOrderAttachmentMapper attachmentMapper;

    private Long adminId;
    private Long workerId;
    private Long employeeId;
    private Long qualificationTypeId;
    private Long workOrderId;

    @BeforeEach
    void setUp() {
        SysRole adminRole = insertRole("SYSTEM_ADMIN", "System Admin", "ALL", 1, 1);
        SysRole workerRole = insertRole("MAINTAINER", "Maintainer", "SELF", 0, 1);
        SysUser admin = insertUser("qual_sync_admin", "Admin", "13980000000", 1, 1);
        SysUser worker = insertUser("qual_sync_worker", "Worker", "13980000001", 0, 1);
        adminId = admin.getId();
        workerId = worker.getId();
        insertUserRole(adminId, adminRole.getId());
        insertUserRole(workerId, workerRole.getId());

        QualificationType type = new QualificationType();
        type.setQualificationCode("OFFSHORE");
        type.setQualificationName("Offshore certificate");
        type.setWarningDays(30);
        type.setRequiredFlag(1);
        type.setEnabledFlag(1);
        type.setCreatedAt(LocalDateTime.now());
        type.setUpdatedAt(LocalDateTime.now());
        type.setDeletedFlag(0);
        qualificationTypeMapper.insert(type);
        qualificationTypeId = type.getId();

        EmployeeInfo employee = new EmployeeInfo();
        employee.setUserId(workerId);
        employee.setEmployeeNo("EMP-QS-001");
        employee.setRealName("Worker");
        employee.setPhone("13980000001");
        employee.setPositionName("Maintainer");
        employee.setEmployeeStatus("ACTIVE");
        employee.setCreatedAt(LocalDateTime.now());
        employee.setUpdatedAt(LocalDateTime.now());
        employee.setDeletedFlag(0);
        employeeMapper.insert(employee);
        employeeId = employee.getId();

        ProjectInfo project = new ProjectInfo();
        project.setProjectCode("PRJ-QS");
        project.setProjectName("Qualification Sync Project");
        project.setProjectManagerId(adminId);
        project.setProjectStatus("ACTIVE");
        project.setSortOrder(1);
        project.setVersion(1);
        project.setSyncStatus("SYNCED");
        project.setCreatedAt(LocalDateTime.now());
        project.setUpdatedAt(LocalDateTime.now());
        project.setDeletedFlag(0);
        projectInfoMapper.insert(project);

        WorkOrder workOrder = new WorkOrder();
        workOrder.setWorkOrderNo("WO-QS-001");
        workOrder.setProjectId(project.getId());
        workOrder.setWorkTitle("Qualification sync work order");
        workOrder.setWorkType("REPAIR");
        workOrder.setWorkLocation("Platform A");
        workOrder.setWorkContent("Repair content");
        workOrder.setMaintainerId(workerId);
        workOrder.setStatus("IN_PROGRESS");
        workOrder.setPriority("NORMAL");
        workOrder.setAcceptanceRequired(1);
        workOrder.setSourceType("PC");
        workOrder.setVersion(1);
        workOrder.setSyncStatus("SYNCED");
        workOrder.setCreatedAt(LocalDateTime.now());
        workOrder.setUpdatedAt(LocalDateTime.now());
        workOrder.setDeletedFlag(0);
        workOrderMapper.insert(workOrder);
        workOrderId = workOrder.getId();

        WorkOrderAssignment assignment = new WorkOrderAssignment();
        assignment.setWorkOrderId(workOrderId);
        assignment.setAssignerId(adminId);
        assignment.setAssigneeId(workerId);
        assignment.setAssignmentRole("MAINTAINER");
        assignment.setAssignmentStatus("ACCEPTED");
        assignment.setAssignedAt(LocalDateTime.now());
        assignment.setVersion(1);
        assignment.setSyncStatus("SYNCED");
        assignment.setCreatedAt(LocalDateTime.now());
        assignment.setUpdatedAt(LocalDateTime.now());
        assignment.setDeletedFlag(0);
        assignmentMapper.insert(assignment);
    }

    @Test
    void certificateWarningDispatchCheckAndMobileStatusWork() throws Exception {
        String adminToken = login("qual_sync_admin", "PC");
        String workerToken = login("qual_sync_worker", "MOBILE");

        Map<String, Object> cert = Map.of(
                "qualificationTypeId", qualificationTypeId,
                "certificateNo", "CERT-QS-001",
                "certificateName", "Offshore certificate",
                "validFrom", LocalDate.now().minusDays(10).toString(),
                "validTo", LocalDate.now().plusDays(5).toString(),
                "validStatus", "VALID",
                "fileId", "cert-file-001");
        mockMvc.perform(post("/api/admin/employees/{id}/certificates", employeeId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cert)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.certificateNo").value("CERT-QS-001"));

        mockMvc.perform(get("/api/admin/certificates/warnings").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].validStatus").value("EXPIRING"));

        mockMvc.perform(get("/api/admin/work-orders/{id}/qualification-candidates", workOrderId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].checkResult").value("EXPIRING"));
        assertThat(qualificationCheckMapper.selectAll()).isNotEmpty();

        mockMvc.perform(get("/api/mobile/my/qualification-status").header("Authorization", "Bearer " + workerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.overallStatus").value("EXPIRING"));
        assertThat(certificateMapper.selectAll()).hasSize(1);
    }

    @Test
    void syncDevicePullPushAttachmentConflictAndResolveWork() throws Exception {
        String adminToken = login("qual_sync_admin", "PC");
        String workerToken = login("qual_sync_worker", "MOBILE");

        Map<String, Object> device = Map.of("deviceId", "android-qs-001", "deviceName", "Android Test", "platform", "ANDROID");
        mockMvc.perform(post("/api/sync/device/register")
                        .header("Authorization", "Bearer " + workerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(device)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.deviceId").value("android-qs-001"));
        mockMvc.perform(post("/api/sync/device/register")
                        .header("Authorization", "Bearer " + workerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(device)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.deviceId").value("android-qs-001"));
        assertThat(deviceMapper.selectAll()).extracting(DeviceInfo::getDeviceId).containsExactly("android-qs-001");

        mockMvc.perform(post("/api/sync/pull")
                        .header("Authorization", "Bearer " + workerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("deviceId", "android-qs-001"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].entityType").value("WORK_ORDER"));

        Long recordId = pushRecord(workerToken, "local-rec-sync-001", 1).at("/data/items/0/serverId").asLong();
        assertThat(recordMapper.selectById(recordId).getServerId()).isEqualTo(recordId);

        Map<String, Object> attachmentPayload = Map.of(
                "workOrderId", workOrderId,
                "recordId", recordId,
                "fileId", "file-sync-001",
                "attachmentName", "photo.jpg");
        Map<String, Object> attachmentItem = Map.of(
                "moduleType", "WORK_ORDER",
                "entityType", "WORK_ORDER_ATTACHMENT",
                "actionType", "CREATE",
                "localId", "local-att-sync-001",
                "version", 1,
                "fileId", "file-sync-001",
                "payload", attachmentPayload);
        mockMvc.perform(post("/api/sync/push")
                        .header("Authorization", "Bearer " + workerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "deviceId", "android-qs-001",
                                "batchId", "batch-attachment-001",
                                "items", List.of(attachmentItem)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.successCount").value(1));
        assertThat(attachmentMapper.selectAll()).hasSize(1);

        JsonNode conflictBody = pushRecordUpdate(workerToken, recordId, 0);
        Long conflictId = conflictBody.at("/data/items/0/conflictId").asLong();
        assertThat(conflictBody.at("/data/conflictCount").asInt()).isEqualTo(1);
        assertThat(syncConflictMapper.selectById(conflictId).getResolveStatus()).isEqualTo("PENDING");

        mockMvc.perform(get("/api/admin/sync/conflicts").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(conflictId));

        mockMvc.perform(post("/api/admin/sync/conflicts/{id}/resolve", conflictId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "resolveStrategy", "KEEP_SERVER",
                                "resolveComment", "keep server version"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.resolveStatus").value("RESOLVED"));
        assertThat(operationLogMapper.selectAll())
                .extracting(OperationLog::getOperationType)
                .contains("RESOLVE_SYNC_CONFLICT");
        mockMvc.perform(get("/api/admin/sync/audit-trail").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.action == 'RESOLVE_SYNC_CONFLICT')]").exists());
        assertThat(syncTaskMapper.selectAll()).isNotEmpty();
        assertThat(syncLogMapper.selectAll()).isNotEmpty();
    }

    private JsonNode pushRecord(String token, String localId, int version) throws Exception {
        Map<String, Object> payload = Map.of(
                "workOrderId", workOrderId,
                "recordType", "DAILY",
                "constructionDesc", "offline record",
                "abnormalFlag", 0);
        Map<String, Object> item = Map.of(
                "moduleType", "WORK_ORDER",
                "entityType", "WORK_ORDER_RECORD",
                "actionType", "CREATE",
                "localId", localId,
                "version", version,
                "payload", payload);
        String body = mockMvc.perform(post("/api/sync/push")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "deviceId", "android-qs-001",
                                "batchId", "batch-record-001",
                                "items", List.of(item)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.successCount").value(1))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body);
    }

    private JsonNode pushRecordUpdate(String token, Long recordId, int version) throws Exception {
        Map<String, Object> item = Map.of(
                "moduleType", "WORK_ORDER",
                "entityType", "WORK_ORDER_RECORD",
                "actionType", "UPDATE",
                "localId", "local-rec-sync-001",
                "serverId", recordId,
                "version", version,
                "payload", Map.of("constructionDesc", "stale update"));
        String body = mockMvc.perform(post("/api/sync/push")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "deviceId", "android-qs-001",
                                "batchId", "batch-conflict-001",
                                "items", List.of(item)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.conflictCount").value(1))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body);
    }

    private String login(String username, String platform) throws Exception {
        LoginRequest request = new LoginRequest();
        request.setLoginName(username);
        request.setPassword("123456");
        request.setPlatform(platform);
        String body = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).at("/data/token").asText();
    }

    private SysUser insertUser(String username, String realName, String phone, Integer pcEnabled, Integer mobileEnabled) {
        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPasswordHash("{noop}123456");
        user.setRealName(realName);
        user.setPhone(phone);
        user.setAccountStatus("ACTIVE");
        user.setPcEnabled(pcEnabled);
        user.setMobileEnabled(mobileEnabled);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setDeletedFlag(0);
        sysUserMapper.insert(user);
        return user;
    }

    private SysRole insertRole(String roleCode, String roleName, String dataScope, Integer pcEnabled, Integer mobileEnabled) {
        SysRole role = new SysRole();
        role.setRoleCode(roleCode);
        role.setRoleName(roleName);
        role.setRoleType("SYSTEM_ADMIN".equals(roleCode) ? "SYSTEM" : "BUSINESS");
        role.setDataScope(dataScope);
        role.setPcEnabled(pcEnabled);
        role.setMobileEnabled(mobileEnabled);
        role.setSortOrder(1);
        role.setStatus("ACTIVE");
        role.setCreatedAt(LocalDateTime.now());
        role.setUpdatedAt(LocalDateTime.now());
        role.setDeletedFlag(0);
        sysRoleMapper.insert(role);
        return role;
    }

    private void insertUserRole(Long userId, Long roleId) {
        SysUserRole relation = new SysUserRole();
        relation.setUserId(userId);
        relation.setRoleId(roleId);
        relation.setCreatedAt(LocalDateTime.now());
        relation.setUpdatedAt(LocalDateTime.now());
        relation.setDeletedFlag(0);
        sysUserRoleMapper.insert(relation);
    }
}
