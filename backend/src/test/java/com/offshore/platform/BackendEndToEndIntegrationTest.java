package com.offshore.platform;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.offshore.platform.dto.auth.LoginRequest;
import com.offshore.platform.entity.AiResult;
import com.offshore.platform.entity.EmployeeCertificate;
import com.offshore.platform.entity.EmployeeInfo;
import com.offshore.platform.entity.FileStorage;
import com.offshore.platform.entity.MaterialInfo;
import com.offshore.platform.entity.ProjectInfo;
import com.offshore.platform.entity.QualificationType;
import com.offshore.platform.entity.SyncConflict;
import com.offshore.platform.entity.SysRole;
import com.offshore.platform.entity.SysUser;
import com.offshore.platform.entity.SysUserRole;
import com.offshore.platform.entity.WorkOrder;
import com.offshore.platform.entity.WorkOrderMaterial;
import com.offshore.platform.entity.WorkOrderQualificationCheck;
import com.offshore.platform.mapper.AiResultMapper;
import com.offshore.platform.mapper.EmployeeCertificateMapper;
import com.offshore.platform.mapper.EmployeeInfoMapper;
import com.offshore.platform.mapper.FileStorageMapper;
import com.offshore.platform.mapper.MaterialInfoMapper;
import com.offshore.platform.mapper.OperationLogMapper;
import com.offshore.platform.mapper.ProjectInfoMapper;
import com.offshore.platform.mapper.QualificationTypeMapper;
import com.offshore.platform.mapper.SyncConflictMapper;
import com.offshore.platform.mapper.SysRoleMapper;
import com.offshore.platform.mapper.SysUserMapper;
import com.offshore.platform.mapper.SysUserRoleMapper;
import com.offshore.platform.mapper.WorkOrderMapper;
import com.offshore.platform.mapper.WorkOrderMaterialMapper;
import com.offshore.platform.mapper.WorkOrderMaterialUsageMapper;
import com.offshore.platform.mapper.WorkOrderQualificationCheckMapper;
import com.offshore.platform.mapper.WorkOrderRecordMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
        "spring.flyway.enabled=false",
        "app.file.storage-root=target/test-e2e-files"
})
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Sql(scripts = "/test-schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BackendEndToEndIntegrationTest {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired SysUserMapper sysUserMapper;
    @Autowired SysRoleMapper sysRoleMapper;
    @Autowired SysUserRoleMapper sysUserRoleMapper;
    @Autowired ProjectInfoMapper projectInfoMapper;
    @Autowired WorkOrderMapper workOrderMapper;
    @Autowired WorkOrderMaterialMapper workOrderMaterialMapper;
    @Autowired WorkOrderQualificationCheckMapper qualificationCheckMapper;
    @Autowired MaterialInfoMapper materialInfoMapper;
    @Autowired EmployeeInfoMapper employeeInfoMapper;
    @Autowired EmployeeCertificateMapper certificateMapper;
    @Autowired QualificationTypeMapper qualificationTypeMapper;
    @Autowired FileStorageMapper fileStorageMapper;
    @Autowired WorkOrderRecordMapper recordMapper;
    @Autowired WorkOrderMaterialUsageMapper materialUsageMapper;
    @Autowired SyncConflictMapper syncConflictMapper;
    @Autowired AiResultMapper aiResultMapper;
    @Autowired OperationLogMapper operationLogMapper;

    private Long adminId;
    private Long workerId;
    private Long otherWorkerId;
    private Long otherWorkOrderId;
    private Long projectId;
    private Long workOrderId;
    private Long materialId;
    private Long employeeId;
    private Long qualificationTypeId;
    private Long recordId;
    private Long attachmentId;
    private String photoFileId;
    private Long syncRecordId;
    private Long conflictId;
    private Long aiResultId;
    private String adminToken;
    private String workerToken;
    private String otherWorkerToken;

    @BeforeAll
    void setUpSeedData() {
        SysRole adminRole = insertRole("SYSTEM_ADMIN", "System Admin", "ALL", 1, 1);
        SysRole workerRole = insertRole("MAINTAINER", "Maintainer", "SELF", 0, 1);

        SysUser admin = insertUser("e2e_admin", "E2E Admin", "13910000000", 1, 1);
        SysUser worker = insertUser("e2e_worker", "E2E Worker", "13910000001", 0, 1);
        SysUser other = insertUser("e2e_other_worker", "E2E Other Worker", "13910000002", 0, 1);
        adminId = admin.getId();
        workerId = worker.getId();
        otherWorkerId = other.getId();
        insertUserRole(adminId, adminRole.getId());
        insertUserRole(workerId, workerRole.getId());
        insertUserRole(otherWorkerId, workerRole.getId());

        qualificationTypeId = insertQualificationType();
        employeeId = insertEmployeeForWorker();
        otherWorkOrderId = insertOtherWorkerOrder();
    }

    @Test
    @Order(1)
    void flowOneLoginCurrentUserAndPermissionWork() throws Exception {
        adminToken = login("e2e_admin", "PC");
        workerToken = login("e2e_worker", "MOBILE");
        otherWorkerToken = login("e2e_other_worker", "MOBILE");

        mockMvc.perform(get("/api/auth/current").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("e2e_admin"))
                .andExpect(jsonPath("$.traceId").isString());

        mockMvc.perform(get("/api/admin/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));

        mockMvc.perform(get("/api/mobile/work-orders/{id}", otherWorkOrderId)
                        .header("Authorization", "Bearer " + workerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    @Order(2)
    void flowTwoPcCreatesProjectWorkOrderMaterialsQualificationAndAssignment() throws Exception {
        JsonNode project = performJson(post("/api/admin/projects")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "projectCode", "E2E-PRJ-001",
                        "projectName", "E2E Offshore Project",
                        "projectManagerId", adminId,
                        "projectStatus", "ACTIVE"))));
        projectId = project.at("/data/id").asLong();

        JsonNode material = performJson(post("/api/admin/materials")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "materialCode", "E2E-MAT-001",
                        "materialName", "Sealant",
                        "materialSpec", "20L",
                        "unit", "bucket",
                        "enabledFlag", 1,
                        "traceEnabled", 1,
                        "qrcodeRequired", 0))));
        materialId = material.at("/data/id").asLong();

        JsonNode order = performJson(post("/api/admin/work-orders")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "projectId", projectId,
                        "workTitle", "E2E repair work order",
                        "workType", "REPAIR",
                        "workLocation", "Platform A",
                        "workContent", "Repair and acceptance closed loop",
                        "requiredMaterialDesc", "Sealant x 2",
                        "priority", "NORMAL",
                        "acceptanceRequired", 1))));
        workOrderId = order.at("/data/id").asLong();
        assertThat(workOrderMapper.selectById(workOrderId).getStatus()).isEqualTo("PENDING_ASSIGN");

        bindRequiredMaterial();
        bindRequiredQualificationPlaceholder();

        performJson(post("/api/admin/work-orders/{id}/assign", workOrderId)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "maintainerId", workerId,
                        "assignmentRole", "MAINTAINER",
                        "remark", "E2E assignment"))))
                .at("/data/status").asText();

        mockMvc.perform(get("/api/admin/work-orders/{id}/status-flow", workOrderId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(2));
        assertThat(workOrderMaterialMapper.selectByWorkOrderId(workOrderId)).hasSize(1);
        assertThat(qualificationCheckMapper.selectAll()).hasSize(1);
    }

    @Test
    @Order(3)
    void flowThreeMobileProcessesAssignedWorkOrder() throws Exception {
        mockMvc.perform(get("/api/mobile/work-orders").header("Authorization", "Bearer " + workerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[?(@.id == " + workOrderId + ")]").exists());

        mockMvc.perform(post("/api/mobile/work-orders/{id}/accept", workOrderId)
                        .header("Authorization", "Bearer " + workerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("ASSIGNED"));

        mockMvc.perform(post("/api/mobile/work-orders/{id}/start", workOrderId)
                        .header("Authorization", "Bearer " + workerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"));

        JsonNode record = performJson(post("/api/mobile/work-orders/{id}/records", workOrderId)
                .header("Authorization", "Bearer " + workerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "localId", "e2e-record-local-001",
                        "version", 1,
                        "recordType", "DAILY",
                        "constructionDesc", "E2E construction record",
                        "abnormalFlag", 0,
                        "deviceId", "android-e2e-001"))));
        recordId = record.at("/data/id").asLong();

        mockMvc.perform(post("/api/mobile/work-orders/{id}/submit-acceptance", workOrderId)
                        .header("Authorization", "Bearer " + workerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("submitDesc", "submit for acceptance"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("PENDING_ACCEPTANCE"));
    }

    @Test
    @Order(4)
    void flowFourFileUploadAttachmentAndAuthorizationWork() throws Exception {
        photoFileId = uploadFile(workerToken, "site-photo.jpg", "PHOTO", "site-photo-content");
        FileStorage storage = fileStorageMapper.selectByFileId(photoFileId);
        assertThat(storage).isNotNull();
        assertThat(storage.getFileHash()).hasSize(64);

        JsonNode attachment = performJson(post("/api/mobile/work-orders/{id}/attachments", workOrderId)
                .header("Authorization", "Bearer " + workerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "localId", "e2e-att-local-001",
                        "recordId", recordId,
                        "fileId", photoFileId,
                        "attachmentType", "PHOTO",
                        "attachmentName", "site-photo.jpg",
                        "businessScene", "WORK_RECORD",
                        "watermarkFlag", 1,
                        "watermarkText", "E2E watermark"))));
        attachmentId = attachment.at("/data/id").asLong();

        mockMvc.perform(get("/api/admin/work-orders/{id}/attachments", workOrderId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].fileId").value(photoFileId));

        mockMvc.perform(get("/api/files/{fileId}/download", photoFileId)
                        .header("Authorization", "Bearer " + otherWorkerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    @Order(5)
    void flowFiveSignatureAcceptancePdfLockAndDownloadWork() throws Exception {
        String signatureFileId = uploadFile(workerToken, "signature.png", "SIGNATURE", "signature-content");

        performJson(post("/api/mobile/work-orders/{id}/signatures", workOrderId)
                .header("Authorization", "Bearer " + workerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "fileId", signatureFileId,
                        "signatureRole", "CONSTRUCTION",
                        "signerName", "E2E Worker"))));

        mockMvc.perform(post("/api/mobile/work-orders/{id}/acceptance", workOrderId)
                        .header("Authorization", "Bearer " + workerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "acceptanceStatus", "PENDING",
                                "acceptanceResult", "submitted",
                                "recordSummary", "records complete"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.lockedFlag").value(0));

        mockMvc.perform(post("/api/admin/work-orders/{id}/acceptance/review", workOrderId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "acceptanceStatus", "PASSED",
                                "acceptanceResult", "passed",
                                "acceptanceOpinion", "accepted"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.acceptanceStatus").value("PASSED"));

        mockMvc.perform(post("/api/admin/work-orders/{id}/pdf/generate", workOrderId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.pdfStatus").value("GENERATED"))
                .andExpect(jsonPath("$.data.lockedFlag").value(1));

        mockMvc.perform(get("/api/admin/work-orders/{id}/pdf/download", workOrderId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", containsString("/api/files/")));
    }

    @Test
    @Order(6)
    void flowSixMaterialTraceAndStatisticsWork() throws Exception {
        mockMvc.perform(post("/api/admin/materials/inbound")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "materialId", materialId,
                                "quantity", 20,
                                "warehouseCode", "DEFAULT",
                                "warehouseName", "E2E Warehouse"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.afterQty").value(20));

        mockMvc.perform(post("/api/admin/materials/outbound")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "materialId", materialId,
                                "quantity", 5,
                                "workOrderId", workOrderId,
                                "warehouseCode", "DEFAULT"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(post("/api/mobile/work-orders/{id}/material-usage", workOrderId)
                        .header("Authorization", "Bearer " + workerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "materialId", materialId,
                                "usedQty", 2,
                                "wasteQty", 0,
                                "returnQty", 0,
                                "localId", "e2e-material-use-001",
                                "deviceId", "android-e2e-001"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.usedQty").value(2));

        mockMvc.perform(get("/api/admin/dashboard/material-statistics")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        assertThat(materialUsageMapper.selectAll()).isNotEmpty();
    }

    @Test
    @Order(7)
    void flowSevenQualificationCertificateDispatchCheckAndWarningWork() throws Exception {
        JsonNode employee = performJson(post("/api/admin/employees")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "userId", workerId,
                        "employeeNo", "E2E-EMP-001",
                        "realName", "E2E Worker",
                        "phone", "13910000001",
                        "positionName", "Maintainer",
                        "employeeStatus", "ACTIVE"))));
        employeeId = employee.at("/data/id").asLong();

        mockMvc.perform(post("/api/admin/employees/{id}/certificates", employeeId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "qualificationTypeId", qualificationTypeId,
                                "certificateNo", "E2E-CERT-001",
                                "certificateName", "Offshore certificate",
                                "validFrom", LocalDate.now().minusDays(10).toString(),
                                "validTo", LocalDate.now().plusDays(5).toString(),
                                "validStatus", "VALID",
                                "fileId", "cert-file-e2e"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(get("/api/admin/work-orders/{id}/qualification-candidates", workOrderId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].checkResult").value("EXPIRING"));

        mockMvc.perform(get("/api/admin/certificates/warnings")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].validStatus").value("EXPIRING"));
        assertThat(certificateMapper.selectAll()).extracting(EmployeeCertificate::getCertificateNo).contains("E2E-CERT-001");
    }

    @Test
    @Order(8)
    void flowEightOfflineSyncPullPushIdempotencyConflictAndResolveWork() throws Exception {
        mockMvc.perform(post("/api/sync/device/register")
                        .header("Authorization", "Bearer " + workerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "deviceId", "android-e2e-001",
                                "deviceName", "E2E Android",
                                "platform", "ANDROID"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(post("/api/sync/pull")
                        .header("Authorization", "Bearer " + workerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("deviceId", "android-e2e-001"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.items[?(@.entityType == 'WORK_ORDER')]").exists());

        JsonNode push = performJson(post("/api/sync/push")
                .header("Authorization", "Bearer " + workerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(syncRecordPushBody("e2e-sync-batch-001", "CREATE", null, 1))));
        syncRecordId = push.at("/data/items/0/serverId").asLong();

        mockMvc.perform(post("/api/sync/push")
                        .header("Authorization", "Bearer " + workerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(syncRecordPushBody("e2e-sync-batch-001", "CREATE", null, 1))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.successCount").value(1));

        JsonNode conflict = performJson(post("/api/sync/push")
                .header("Authorization", "Bearer " + workerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(syncRecordPushBody("e2e-sync-batch-002", "UPDATE", syncRecordId, 0))));
        conflictId = conflict.at("/data/items/0/conflictId").asLong();

        mockMvc.perform(post("/api/admin/sync/conflicts/{id}/resolve", conflictId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "resolveStrategy", "KEEP_SERVER",
                                "resolveComment", "E2E keep server"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.resolveStatus").value("RESOLVED"));
        assertThat(syncConflictMapper.selectById(conflictId)).extracting(SyncConflict::getResolveStatus).isEqualTo("RESOLVED");
    }

    @Test
    @Order(9)
    void flowNineAiAuxiliaryAcceptanceDoesNotChangeWorkOrderStatus() throws Exception {
        String statusBefore = workOrderMapper.selectById(workOrderId).getStatus();
        JsonNode ai = performJson(post("/api/ai/results")
                .header("Authorization", "Bearer " + workerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(aiResultBody())));
        aiResultId = ai.at("/data/id").asLong();

        mockMvc.perform(post("/api/admin/ai/results/{id}/review", aiResultId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "reviewStatus", "CONFIRMED",
                                "confirmedDefectType", "RUST",
                                "reviewOpinion", "E2E confirmed"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.reviewStatus").value("CONFIRMED"));

        assertThat(aiResultMapper.selectById(aiResultId)).extracting(AiResult::getWorkOrderId).isEqualTo(workOrderId);
        assertThat(workOrderMapper.selectById(workOrderId).getStatus()).isEqualTo(statusBefore);
    }

    @Test
    @Order(10)
    void flowTenDashboardAndExcelExportWork() throws Exception {
        assertThat(workOrderMapper.selectById(workOrderId).getStatus()).isEqualTo("COMPLETED");

        mockMvc.perform(get("/api/admin/dashboard/overview")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists());

        mockMvc.perform(get("/api/admin/dashboard/work-order-statistics")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(get("/api/admin/dashboard/material-statistics")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        byte[] excel = mockMvc.perform(get("/api/admin/reports/reconciliation/export")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("reconciliation.xlsx")))
                .andReturn().getResponse().getContentAsByteArray();
        assertThat(excel).isNotEmpty();
        assertThat(operationLogMapper.selectAll()).isNotEmpty();
    }

    private JsonNode performJson(org.springframework.test.web.servlet.RequestBuilder builder) throws Exception {
        String body = mockMvc.perform(builder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.traceId").isString())
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
                .andExpect(jsonPath("$.traceId").isString())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).at("/data/token").asText();
    }

    private String uploadFile(String token, String name, String type, String content) throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", name, MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());
        String body = mockMvc.perform(multipart("/api/files/upload")
                        .file(file)
                        .param("fileType", type)
                        .param("workOrderId", String.valueOf(workOrderId))
                        .param("recordId", recordId == null ? "" : String.valueOf(recordId))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.traceId").isString())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).at("/data/fileId").asText();
    }

    private Map<String, Object> syncRecordPushBody(String batchId, String action, Long serverId, int version) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("workOrderId", workOrderId);
        payload.put("recordType", "DAILY");
        payload.put("constructionDesc", "E2E sync construction record");
        payload.put("abnormalFlag", 0);

        Map<String, Object> item = new LinkedHashMap<>();
        item.put("moduleType", "WORK_ORDER");
        item.put("entityType", "WORK_ORDER_RECORD");
        item.put("actionType", action);
        item.put("localId", "e2e-sync-record-local-001");
        if (serverId != null) {
            item.put("serverId", serverId);
        }
        item.put("version", version);
        item.put("payload", payload);

        Map<String, Object> request = new LinkedHashMap<>();
        request.put("deviceId", "android-e2e-001");
        request.put("batchId", batchId);
        request.put("clientTime", "2026-06-05 16:00:00");
        request.put("appVersion", "1.0.0");
        request.put("items", List.of(item));
        return request;
    }

    private Map<String, Object> aiResultBody() {
        Map<String, Object> box = new LinkedHashMap<>();
        box.put("defectType", "RUST");
        box.put("confidence", 0.82);
        box.put("x", 0.1);
        box.put("y", 0.2);
        box.put("width", 0.3);
        box.put("height", 0.4);
        box.put("boxLabel", "rust");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("localId", "e2e-ai-local-001");
        body.put("workOrderId", workOrderId);
        body.put("recordId", recordId);
        body.put("attachmentId", attachmentId);
        body.put("fileId", photoFileId);
        body.put("modelCode", "E2E-AI");
        body.put("modelVersion", "1.0.0");
        body.put("inferSide", "MOBILE");
        body.put("inferCostMs", 500);
        body.put("defectType", "RUST");
        body.put("confidence", 0.82);
        body.put("suspectedDefectFlag", 1);
        body.put("resultSummary", "Auxiliary rust result");
        body.put("deviceId", "android-e2e-001");
        body.put("boxes", List.of(box));
        return body;
    }

    private void bindRequiredMaterial() {
        MaterialInfo material = materialInfoMapper.selectById(materialId);
        WorkOrderMaterial required = new WorkOrderMaterial();
        required.setWorkOrderId(workOrderId);
        required.setMaterialCode(material.getMaterialCode());
        required.setMaterialName(material.getMaterialName());
        required.setMaterialSpec(material.getMaterialSpec());
        required.setUnit(material.getUnit());
        required.setPlannedQty(BigDecimal.valueOf(2));
        required.setActualQty(BigDecimal.ZERO);
        required.setRequiredFlag(1);
        required.setPrepareStatus("PENDING");
        required.setVersion(1);
        required.setSyncStatus("SYNCED");
        required.setCreatedAt(LocalDateTime.now());
        required.setUpdatedAt(LocalDateTime.now());
        required.setDeletedFlag(0);
        workOrderMaterialMapper.insert(required);
    }

    private void bindRequiredQualificationPlaceholder() {
        WorkOrderQualificationCheck check = new WorkOrderQualificationCheck();
        check.setWorkOrderId(workOrderId);
        check.setEmployeeId(employeeId);
        check.setQualificationTypeId(qualificationTypeId);
        check.setCheckResult("PENDING");
        check.setCheckTime(LocalDateTime.now());
        check.setCheckerId(adminId);
        check.setVersion(1);
        check.setSyncStatus("SYNCED");
        check.setCreatedAt(LocalDateTime.now());
        check.setUpdatedAt(LocalDateTime.now());
        check.setDeletedFlag(0);
        qualificationCheckMapper.insert(check);
    }

    private Long insertQualificationType() {
        QualificationType type = new QualificationType();
        type.setQualificationCode("E2E-OFFSHORE");
        type.setQualificationName("E2E offshore certificate");
        type.setWarningDays(30);
        type.setRequiredFlag(1);
        type.setEnabledFlag(1);
        type.setCreatedAt(LocalDateTime.now());
        type.setUpdatedAt(LocalDateTime.now());
        type.setDeletedFlag(0);
        qualificationTypeMapper.insert(type);
        return type.getId();
    }

    private Long insertEmployeeForWorker() {
        EmployeeInfo employee = new EmployeeInfo();
        employee.setUserId(workerId);
        employee.setEmployeeNo("E2E-EMP-SEED");
        employee.setRealName("E2E Worker");
        employee.setPhone("13910000001");
        employee.setPositionName("Maintainer");
        employee.setEmployeeStatus("ACTIVE");
        employee.setCreatedAt(LocalDateTime.now());
        employee.setUpdatedAt(LocalDateTime.now());
        employee.setDeletedFlag(0);
        employeeInfoMapper.insert(employee);
        return employee.getId();
    }

    private Long insertOtherWorkerOrder() {
        ProjectInfo project = new ProjectInfo();
        project.setProjectCode("E2E-OTHER-PRJ");
        project.setProjectName("E2E Other Project");
        project.setProjectManagerId(adminId);
        project.setProjectStatus("ACTIVE");
        project.setSortOrder(1);
        project.setVersion(1);
        project.setSyncStatus("SYNCED");
        project.setCreatedAt(LocalDateTime.now());
        project.setUpdatedAt(LocalDateTime.now());
        project.setDeletedFlag(0);
        projectInfoMapper.insert(project);

        WorkOrder order = new WorkOrder();
        order.setWorkOrderNo("E2E-OTHER-WO");
        order.setProjectId(project.getId());
        order.setWorkTitle("Other worker order");
        order.setWorkType("REPAIR");
        order.setWorkLocation("Platform B");
        order.setWorkContent("Other worker content");
        order.setMaintainerId(otherWorkerId);
        order.setStatus("ASSIGNED");
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
