package com.offshore.platform;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.offshore.platform.dto.auth.LoginRequest;
import com.offshore.platform.entity.OperationLog;
import com.offshore.platform.entity.ProjectInfo;
import com.offshore.platform.entity.ReportDailySummary;
import com.offshore.platform.entity.SysRole;
import com.offshore.platform.entity.SysUser;
import com.offshore.platform.entity.SysUserRole;
import com.offshore.platform.entity.WorkOrder;
import com.offshore.platform.entity.WorkOrderAssignment;
import com.offshore.platform.entity.WorkOrderAttachment;
import com.offshore.platform.entity.WorkOrderMaterialUsage;
import com.offshore.platform.entity.WorkOrderRecord;
import com.offshore.platform.mapper.AiDefectBoxMapper;
import com.offshore.platform.mapper.AiResultMapper;
import com.offshore.platform.mapper.AiReviewRecordMapper;
import com.offshore.platform.mapper.OperationLogMapper;
import com.offshore.platform.mapper.ProjectInfoMapper;
import com.offshore.platform.mapper.ReportDailySummaryMapper;
import com.offshore.platform.mapper.SysRoleMapper;
import com.offshore.platform.mapper.SysUserMapper;
import com.offshore.platform.mapper.SysUserRoleMapper;
import com.offshore.platform.mapper.WorkOrderAssignmentMapper;
import com.offshore.platform.mapper.WorkOrderAttachmentMapper;
import com.offshore.platform.mapper.WorkOrderMapper;
import com.offshore.platform.mapper.WorkOrderMaterialUsageMapper;
import com.offshore.platform.mapper.WorkOrderRecordMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.LinkedHashMap;
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
class AiDashboardControllerTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private SysUserMapper sysUserMapper;
    @Autowired private SysRoleMapper sysRoleMapper;
    @Autowired private SysUserRoleMapper sysUserRoleMapper;
    @Autowired private ProjectInfoMapper projectMapper;
    @Autowired private WorkOrderMapper workOrderMapper;
    @Autowired private WorkOrderAssignmentMapper assignmentMapper;
    @Autowired private WorkOrderAttachmentMapper attachmentMapper;
    @Autowired private WorkOrderRecordMapper recordMapper;
    @Autowired private WorkOrderMaterialUsageMapper materialUsageMapper;
    @Autowired private ReportDailySummaryMapper reportMapper;
    @Autowired private AiResultMapper aiResultMapper;
    @Autowired private AiDefectBoxMapper aiDefectBoxMapper;
    @Autowired private AiReviewRecordMapper aiReviewRecordMapper;
    @Autowired private OperationLogMapper operationLogMapper;

    private Long adminId;
    private Long managerId;
    private Long workerId;
    private Long projectId;
    private Long otherProjectId;
    private Long workOrderId;
    private Long attachmentId;
    private Long recordId;

    @BeforeEach
    void setUp() {
        SysRole adminRole = insertRole("SYSTEM_ADMIN", "System Admin", "ALL", 1, 1);
        SysRole managerRole = insertRole("PROJECT_MANAGER", "Project Manager", "PROJECT", 1, 0);
        SysRole workerRole = insertRole("MAINTAINER", "Maintainer", "SELF", 0, 1);
        SysUser admin = insertUser("ai_dash_admin", "Admin", "13990000000", 1, 1, null);
        SysUser manager = insertUser("ai_dash_manager", "Manager", "13990000001", 1, 0, null);
        SysUser worker = insertUser("ai_dash_worker", "Worker", "13990000002", 0, 1, null);
        adminId = admin.getId();
        managerId = manager.getId();
        workerId = worker.getId();
        insertUserRole(adminId, adminRole.getId());
        insertUserRole(managerId, managerRole.getId());
        insertUserRole(workerId, workerRole.getId());

        projectId = insertProject("PRJ-AI-1", "AI Project", managerId);
        otherProjectId = insertProject("PRJ-AI-2", "Other Project", adminId);
        manager.setPrimaryProjectId(projectId);
        sysUserMapper.updateById(manager);

        workOrderId = insertWorkOrder("WO-AI-001", projectId, "IN_PROGRESS", workerId);
        insertWorkOrder("WO-AI-002", projectId, "PENDING_ACCEPTANCE", workerId);
        insertWorkOrder("WO-AI-003", projectId, "COMPLETED", workerId);
        insertWorkOrder("WO-AI-004", otherProjectId, "IN_PROGRESS", workerId);

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

        recordId = insertRecord(workOrderId, projectId, workerId, 1);

        WorkOrderAttachment attachment = new WorkOrderAttachment();
        attachment.setWorkOrderId(workOrderId);
        attachment.setRecordId(recordId);
        attachment.setFileId("photo-ai-001");
        attachment.setAttachmentType("PHOTO");
        attachment.setAttachmentName("photo.jpg");
        attachment.setCaptureTime(LocalDateTime.now());
        attachment.setCaptureUserId(workerId);
        attachment.setUploadStatus("UPLOADED");
        attachment.setPreviewStatus("AVAILABLE");
        attachment.setMobileCacheStatus("NOT_CACHED");
        attachment.setVersion(1);
        attachment.setSyncStatus("SYNCED");
        attachment.setCreatedAt(LocalDateTime.now());
        attachment.setUpdatedAt(LocalDateTime.now());
        attachment.setDeletedFlag(0);
        attachmentMapper.insert(attachment);
        attachmentId = attachment.getId();

        insertMaterialUsage(workOrderId, projectId);
        insertReport(projectId, BigDecimal.valueOf(1200));
        insertReport(otherProjectId, BigDecimal.valueOf(900));
    }

    @Test
    void aiResultSubmissionBindsPhotoAndDoesNotChangeWorkOrderStatus() throws Exception {
        String workerToken = login("ai_dash_worker", "MOBILE");
        String adminToken = login("ai_dash_admin", "PC");
        Long resultId = createAiResult(workerToken);

        assertThat(aiResultMapper.selectById(resultId).getReviewStatus()).isEqualTo("PENDING_REVIEW");
        assertThat(aiDefectBoxMapper.selectAll()).hasSize(1);
        assertThat(attachmentMapper.selectById(attachmentId).getAiResultId()).isEqualTo(resultId);
        assertThat(workOrderMapper.selectById(workOrderId).getStatus()).isEqualTo("IN_PROGRESS");

        mockMvc.perform(get("/api/mobile/work-orders/{id}/ai-results", workOrderId)
                        .header("Authorization", "Bearer " + workerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].auxiliaryNotice").exists());

        mockMvc.perform(post("/api/admin/ai/results/{id}/review", resultId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "reviewStatus", "CONFIRMED",
                                "confirmedDefectType", "RUST",
                                "reviewOpinion", "confirmed by human"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reviewStatus").value("CONFIRMED"));

        assertThat(aiReviewRecordMapper.selectAll()).hasSize(1);
        assertThat(operationLogMapper.selectAll())
                .extracting(OperationLog::getOperationType)
                .contains("REVIEW_AI_RESULT");
        assertThat(workOrderMapper.selectById(workOrderId).getStatus()).isEqualTo("IN_PROGRESS");
    }

    @Test
    void aiModelCreateAndActivateWorks() throws Exception {
        String adminToken = login("ai_dash_admin", "PC");
        String body = mockMvc.perform(post("/api/admin/ai/models")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "modelCode", "YOLO-RUST",
                                "modelName", "Rust Detector",
                                "modelVersion", "1.0.0",
                                "confidenceThreshold", 0.7))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.activeFlag").value(0))
                .andReturn().getResponse().getContentAsString();
        Long modelId = objectMapper.readTree(body).at("/data/id").asLong();

        mockMvc.perform(put("/api/admin/ai/models/{id}/activate", modelId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.activeFlag").value(1));
    }

    @Test
    void dashboardOverviewProjectScopeAndExcelExportWork() throws Exception {
        String adminToken = login("ai_dash_admin", "PC");
        String managerToken = login("ai_dash_manager", "PC");

        mockMvc.perform(get("/api/admin/dashboard/overview").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.inProgressWorkOrderCount").value(2))
                .andExpect(jsonPath("$.data.pendingAcceptanceWorkOrderCount").value(1))
                .andExpect(jsonPath("$.data.todayAttendanceCount").value(1))
                .andExpect(jsonPath("$.data.certificateExpiringCount").exists())
                .andExpect(jsonPath("$.data.inventoryWarningCount").exists())
                .andExpect(jsonPath("$.data.pendingConflictCount").exists())
                .andExpect(jsonPath("$.data.pendingAiReviewCount").exists());

        mockMvc.perform(get("/api/admin/dashboard/overview").header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.inProgressWorkOrderCount").value(1))
                .andExpect(jsonPath("$.data.pendingAcceptanceWorkOrderCount").value(1));

        byte[] bytes = mockMvc.perform(get("/api/admin/reports/reconciliation/export")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("reconciliation.xlsx")))
                .andReturn().getResponse().getContentAsByteArray();
        assertThat(bytes).isNotEmpty();
        assertThat(operationLogMapper.selectAll())
                .extracting(OperationLog::getOperationType)
                .contains("EXPORT_RECONCILIATION");
    }

    private Long createAiResult(String token) throws Exception {
        Map<String, Object> box = Map.of(
                "defectType", "RUST",
                "confidence", 0.82,
                "x", 0.1,
                "y", 0.2,
                "width", 0.3,
                "height", 0.4,
                "boxLabel", "rust area");
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("workOrderId", workOrderId);
        request.put("recordId", recordId);
        request.put("attachmentId", attachmentId);
        request.put("fileId", "photo-ai-001");
        request.put("modelCode", "YOLO-RUST");
        request.put("modelVersion", "1.0.0");
        request.put("inferCostMs", 800);
        request.put("defectType", "RUST");
        request.put("confidence", 0.82);
        request.put("suspectedDefectFlag", 1);
        request.put("resultSummary", "AI auxiliary rust suspicion");
        request.put("boxes", List.of(box));
        String body = mockMvc.perform(post("/api/ai/results")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reviewStatus").value("PENDING_REVIEW"))
                .andExpect(jsonPath("$.data.boxes.length()").value(1))
                .andReturn().getResponse().getContentAsString();
        JsonNode json = objectMapper.readTree(body);
        return json.at("/data/id").asLong();
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

    private SysUser insertUser(String username, String realName, String phone, Integer pcEnabled, Integer mobileEnabled,
            Long primaryProjectId) {
        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPasswordHash("{noop}123456");
        user.setRealName(realName);
        user.setPhone(phone);
        user.setAccountStatus("ACTIVE");
        user.setPcEnabled(pcEnabled);
        user.setMobileEnabled(mobileEnabled);
        user.setPrimaryProjectId(primaryProjectId);
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

    private Long insertProject(String code, String name, Long managerId) {
        ProjectInfo project = new ProjectInfo();
        project.setProjectCode(code);
        project.setProjectName(name);
        project.setProjectManagerId(managerId);
        project.setProjectStatus("ACTIVE");
        project.setSortOrder(1);
        project.setVersion(1);
        project.setSyncStatus("SYNCED");
        project.setCreatedAt(LocalDateTime.now());
        project.setUpdatedAt(LocalDateTime.now());
        project.setDeletedFlag(0);
        projectMapper.insert(project);
        return project.getId();
    }

    private Long insertWorkOrder(String no, Long projectId, String status, Long maintainerId) {
        WorkOrder order = new WorkOrder();
        order.setWorkOrderNo(no);
        order.setProjectId(projectId);
        order.setWorkTitle(no);
        order.setWorkType("REPAIR");
        order.setWorkLocation("Platform A");
        order.setWorkContent("content");
        order.setMaintainerId(maintainerId);
        order.setStatus(status);
        order.setPriority("NORMAL");
        order.setAcceptanceRequired(1);
        order.setSourceType("PC");
        order.setVersion(1);
        order.setSyncStatus("SYNCED");
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        order.setDeletedFlag(0);
        workOrderMapper.insert(order);
        return order.getId();
    }

    private Long insertRecord(Long workOrderId, Long projectId, Long userId, Integer abnormalFlag) {
        WorkOrderRecord record = new WorkOrderRecord();
        record.setWorkOrderId(workOrderId);
        record.setProjectId(projectId);
        record.setRecordNo("REC-AI-001");
        record.setRecordType("DAILY");
        record.setConstructionTime(LocalDateTime.now());
        record.setConstructionUserId(userId);
        record.setConstructionUserName("Worker");
        record.setConstructionDesc("record");
        record.setAbnormalFlag(abnormalFlag);
        record.setAttachmentCount(1);
        record.setAiResultCount(0);
        record.setRecordStatus("SUBMITTED");
        record.setVersion(1);
        record.setSyncStatus("SYNCED");
        record.setCreatedAt(LocalDateTime.now());
        record.setUpdatedAt(LocalDateTime.now());
        record.setDeletedFlag(0);
        recordMapper.insert(record);
        return record.getId();
    }

    private void insertMaterialUsage(Long workOrderId, Long projectId) {
        WorkOrderMaterialUsage usage = new WorkOrderMaterialUsage();
        usage.setUsageNo("USG-AI-001");
        usage.setWorkOrderId(workOrderId);
        usage.setWorkOrderNo("WO-AI-001");
        usage.setProjectId(projectId);
        usage.setMaterialId(1L);
        usage.setMaterialCode("MAT-001");
        usage.setMaterialName("Coating");
        usage.setUnit("kg");
        usage.setUsedQty(BigDecimal.TEN);
        usage.setCostAmount(BigDecimal.valueOf(300));
        usage.setUsageTime(LocalDateTime.now());
        usage.setUsageUserId(workerId);
        usage.setUsageUserName("Worker");
        usage.setVersion(1);
        usage.setSyncStatus("SYNCED");
        usage.setCreatedAt(LocalDateTime.now());
        usage.setUpdatedAt(LocalDateTime.now());
        usage.setDeletedFlag(0);
        materialUsageMapper.insert(usage);
    }

    private void insertReport(Long projectId, BigDecimal output) {
        ReportDailySummary report = new ReportDailySummary();
        report.setSummaryDate(LocalDate.now());
        report.setProjectId(projectId);
        report.setProjectName(projectId.equals(this.projectId) ? "AI Project" : "Other Project");
        report.setWorkOrderTotal(1);
        report.setWorkOrderCompleted(1);
        report.setOutputValue(output);
        report.setGeneratedAt(LocalDateTime.now());
        report.setCreatedAt(LocalDateTime.now());
        report.setUpdatedAt(LocalDateTime.now());
        report.setDeletedFlag(0);
        reportMapper.insert(report);
    }
}
