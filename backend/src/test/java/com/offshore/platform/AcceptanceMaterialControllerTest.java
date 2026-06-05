package com.offshore.platform;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.offshore.platform.dto.acceptance.AcceptanceRequest;
import com.offshore.platform.dto.acceptance.AcceptanceReviewRequest;
import com.offshore.platform.dto.acceptance.SignatureRequest;
import com.offshore.platform.dto.auth.LoginRequest;
import com.offshore.platform.dto.material.MaterialInoutRequest;
import com.offshore.platform.dto.material.MaterialRequest;
import com.offshore.platform.dto.material.MaterialUsageRequest;
import com.offshore.platform.entity.MaterialInfo;
import com.offshore.platform.entity.OperationLog;
import com.offshore.platform.entity.ProjectInfo;
import com.offshore.platform.entity.SysRole;
import com.offshore.platform.entity.SysUser;
import com.offshore.platform.entity.SysUserRole;
import com.offshore.platform.entity.WorkOrder;
import com.offshore.platform.entity.WorkOrderAssignment;
import com.offshore.platform.mapper.MaterialInfoMapper;
import com.offshore.platform.mapper.MaterialInventoryMapper;
import com.offshore.platform.mapper.OperationLogMapper;
import com.offshore.platform.mapper.ProjectInfoMapper;
import com.offshore.platform.mapper.SysRoleMapper;
import com.offshore.platform.mapper.SysUserMapper;
import com.offshore.platform.mapper.SysUserRoleMapper;
import com.offshore.platform.mapper.WorkOrderAssignmentMapper;
import com.offshore.platform.mapper.WorkOrderMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = {
        "spring.flyway.enabled=false",
        "app.file.storage-root=target/test-acceptance-material-files"
})
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Sql(scripts = "/test-schema.sql")
@Transactional
class AcceptanceMaterialControllerTest {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired SysUserMapper sysUserMapper;
    @Autowired SysRoleMapper sysRoleMapper;
    @Autowired SysUserRoleMapper sysUserRoleMapper;
    @Autowired ProjectInfoMapper projectInfoMapper;
    @Autowired WorkOrderMapper workOrderMapper;
    @Autowired WorkOrderAssignmentMapper assignmentMapper;
    @Autowired MaterialInfoMapper materialInfoMapper;
    @Autowired MaterialInventoryMapper inventoryMapper;
    @Autowired OperationLogMapper operationLogMapper;

    private Long adminId;
    private Long workerId;
    private Long workOrderId;

    @BeforeEach
    void setUp() {
        SysRole adminRole = insertRole("SYSTEM_ADMIN", "系统管理员", "ALL", 1, 0);
        SysRole workerRole = insertRole("MAINTAINER", "维修工", "SELF", 0, 1);
        SysUser admin = insertUser("accept_admin", "验收管理员", "13970000000", 1, 0);
        SysUser worker = insertUser("accept_worker", "验收维修工", "13970000001", 0, 1);
        adminId = admin.getId();
        workerId = worker.getId();
        insertUserRole(adminId, adminRole.getId());
        insertUserRole(workerId, workerRole.getId());

        ProjectInfo project = new ProjectInfo();
        project.setProjectCode("PRJ-ACC-MAT");
        project.setProjectName("验收物料项目");
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
        order.setWorkOrderNo("WO-ACC-MAT-001");
        order.setProjectId(project.getId());
        order.setWorkTitle("验收物料工单");
        order.setWorkType("REPAIR");
        order.setWorkLocation("A平台");
        order.setWorkContent("验收物料测试");
        order.setMaintainerId(workerId);
        order.setStatus("PENDING_ACCEPTANCE");
        order.setPriority("NORMAL");
        order.setAcceptanceRequired(1);
        order.setSourceType("PC");
        order.setVersion(1);
        order.setSyncStatus("SYNCED");
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        order.setDeletedFlag(0);
        workOrderMapper.insert(order);
        workOrderId = order.getId();

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
    void signatureAcceptancePdfAndLockedAcceptanceFlow() throws Exception {
        String mobileToken = login("accept_worker", "MOBILE");
        String adminToken = login("accept_admin", "PC");
        String signatureFileId = uploadFile(mobileToken, "signature.png", "SIGNATURE");

        AcceptanceRequest acceptance = new AcceptanceRequest();
        acceptance.acceptanceStatus = "PENDING";
        acceptance.acceptanceResult = "现场施工完成，待复核";
        acceptance.recordSummary = "完成防腐层修复";
        mockMvc.perform(post("/api/mobile/work-orders/{workOrderId}/acceptance", workOrderId)
                        .header("Authorization", "Bearer " + mobileToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(acceptance)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.acceptanceStatus").value("PENDING"))
                .andExpect(jsonPath("$.data.lockedFlag").value(0));

        SignatureRequest signature = new SignatureRequest();
        signature.fileId = signatureFileId;
        signature.signatureRole = "CONSTRUCTION";
        signature.signerName = "验收维修工";
        mockMvc.perform(post("/api/mobile/work-orders/{workOrderId}/signatures", workOrderId)
                        .header("Authorization", "Bearer " + mobileToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signature)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fileId").value(signatureFileId))
                .andExpect(jsonPath("$.data.signatureStatus").value("SIGNED"));

        AcceptanceReviewRequest review = new AcceptanceReviewRequest();
        review.acceptanceStatus = "PASSED";
        review.acceptanceResult = "验收通过";
        review.acceptanceOpinion = "符合现场验收要求";
        mockMvc.perform(post("/api/admin/work-orders/{workOrderId}/acceptance/review", workOrderId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(review)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.acceptanceStatus").value("PASSED"));

        mockMvc.perform(post("/api/admin/work-orders/{workOrderId}/pdf/generate", workOrderId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.pdfStatus").value("GENERATED"))
                .andExpect(jsonPath("$.data.lockedFlag").value(1))
                .andExpect(jsonPath("$.data.fileId").isString());

        mockMvc.perform(get("/api/admin/work-orders/{workOrderId}/pdf/download", workOrderId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/api/files/")));
        assertThat(operationLogMapper.selectAll())
                .extracting(OperationLog::getOperationType)
                .contains("DOWNLOAD_PDF");

        AcceptanceRequest lockedUpdate = new AcceptanceRequest();
        lockedUpdate.acceptanceStatus = "PENDING";
        lockedUpdate.acceptanceResult = "锁定后修改";
        mockMvc.perform(post("/api/mobile/work-orders/{workOrderId}/acceptance", workOrderId)
                        .header("Authorization", "Bearer " + mobileToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(lockedUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40001));
    }

    @Test
    void materialInboundOutboundInsufficientAndWorkOrderUsageFlow() throws Exception {
        String adminToken = login("accept_admin", "PC");
        String mobileToken = login("accept_worker", "MOBILE");
        Long materialId = createMaterial("MAT-PAINT-001");

        MaterialInoutRequest inbound = new MaterialInoutRequest();
        inbound.materialId = materialId;
        inbound.quantity = new BigDecimal("10");
        inbound.warehouseCode = "WH-A";
        inbound.warehouseName = "A仓库";
        mockMvc.perform(post("/api/admin/materials/inbound")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inbound)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.inoutType").value("IN"))
                .andExpect(jsonPath("$.data.afterQty").value(10));

        MaterialInoutRequest outbound = new MaterialInoutRequest();
        outbound.materialId = materialId;
        outbound.quantity = new BigDecimal("3");
        outbound.warehouseCode = "WH-A";
        mockMvc.perform(post("/api/admin/materials/outbound")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(outbound)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.inoutType").value("OUT"))
                .andExpect(jsonPath("$.data.afterQty").value(7));

        MaterialInoutRequest tooMuch = new MaterialInoutRequest();
        tooMuch.materialId = materialId;
        tooMuch.quantity = new BigDecimal("99");
        tooMuch.warehouseCode = "WH-A";
        mockMvc.perform(post("/api/admin/materials/outbound")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tooMuch)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(60001));

        MaterialInoutRequest mobileStock = new MaterialInoutRequest();
        mobileStock.materialId = materialId;
        mobileStock.quantity = new BigDecimal("5");
        mockMvc.perform(post("/api/admin/materials/inbound")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mobileStock)))
                .andExpect(status().isOk());

        MaterialUsageRequest usage = new MaterialUsageRequest();
        usage.materialId = materialId;
        usage.usedQty = new BigDecimal("2");
        usage.wasteQty = BigDecimal.ZERO;
        usage.returnQty = BigDecimal.ZERO;
        usage.localId = "local-use-001";
        usage.deviceId = "android-material-device";
        mockMvc.perform(post("/api/mobile/work-orders/{workOrderId}/material-usage", workOrderId)
                        .header("Authorization", "Bearer " + mobileToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usage)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.serverId").isNumber())
                .andExpect(jsonPath("$.data.syncStatus").value("SYNCED"))
                .andExpect(jsonPath("$.data.usedQty").value(2));

        mockMvc.perform(get("/api/admin/work-orders/{workOrderId}/material-usage", workOrderId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));

        assertThat(inventoryMapper.selectByMaterialId(materialId)).isNotEmpty();
    }

    private Long createMaterial(String code) {
        MaterialInfo material = new MaterialInfo();
        material.setMaterialCode(code);
        material.setMaterialName("防腐涂料");
        material.setMaterialSpec("20L");
        material.setUnit("桶");
        material.setEnabledFlag(1);
        material.setTraceEnabled(1);
        material.setQrcodeRequired(0);
        material.setCreatedAt(LocalDateTime.now());
        material.setUpdatedAt(LocalDateTime.now());
        material.setDeletedFlag(0);
        materialInfoMapper.insert(material);
        return material.getId();
    }

    private String uploadFile(String token, String name, String type) throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", name, MediaType.IMAGE_PNG_VALUE, "signature-content".getBytes());
        String body = mockMvc.perform(multipart("/api/files/upload")
                        .file(file)
                        .param("fileType", type)
                        .param("workOrderId", String.valueOf(workOrderId))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).at("/data/fileId").asText();
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
