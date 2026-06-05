package com.offshore.platform;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.offshore.platform.dto.auth.LoginRequest;
import com.offshore.platform.dto.file.AttachmentBindRequest;
import com.offshore.platform.entity.FileStorage;
import com.offshore.platform.entity.OperationLog;
import com.offshore.platform.entity.ProjectInfo;
import com.offshore.platform.entity.SysRole;
import com.offshore.platform.entity.SysUser;
import com.offshore.platform.entity.SysUserRole;
import com.offshore.platform.entity.WorkOrder;
import com.offshore.platform.entity.WorkOrderAssignment;
import com.offshore.platform.mapper.FileStorageMapper;
import com.offshore.platform.mapper.OperationLogMapper;
import com.offshore.platform.mapper.ProjectInfoMapper;
import com.offshore.platform.mapper.SysRoleMapper;
import com.offshore.platform.mapper.SysUserMapper;
import com.offshore.platform.mapper.SysUserRoleMapper;
import com.offshore.platform.mapper.WorkOrderAssignmentMapper;
import com.offshore.platform.mapper.WorkOrderAttachmentMapper;
import com.offshore.platform.mapper.WorkOrderMapper;
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
        "app.file.storage-root=target/test-file-uploads"
})
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Sql(scripts = "/test-schema.sql")
@Transactional
class FileControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Autowired
    private ProjectInfoMapper projectInfoMapper;

    @Autowired
    private WorkOrderMapper workOrderMapper;

    @Autowired
    private WorkOrderAssignmentMapper workOrderAssignmentMapper;

    @Autowired
    private FileStorageMapper fileStorageMapper;

    @Autowired
    private WorkOrderAttachmentMapper workOrderAttachmentMapper;

    @Autowired
    private OperationLogMapper operationLogMapper;

    private Long adminId;
    private Long maintainerId;
    private Long otherMaintainerId;
    private Long workOrderId;

    @BeforeEach
    void setUp() {
        SysRole adminRole = insertRole("SYSTEM_ADMIN", "系统管理员", "ALL", 1, 0);
        SysRole maintainerRole = insertRole("MAINTAINER", "维修工", "SELF", 0, 1);
        SysUser admin = insertUser("file_admin", "文件管理员", "13960000000", 1, 0);
        SysUser maintainer = insertUser("file_worker", "现场维修工", "13960000001", 0, 1);
        SysUser other = insertUser("file_other", "其他维修工", "13960000002", 0, 1);
        adminId = admin.getId();
        maintainerId = maintainer.getId();
        otherMaintainerId = other.getId();
        insertUserRole(adminId, adminRole.getId());
        insertUserRole(maintainerId, maintainerRole.getId());
        insertUserRole(otherMaintainerId, maintainerRole.getId());

        ProjectInfo project = new ProjectInfo();
        project.setProjectCode("PRJ-FILE");
        project.setProjectName("文件附件项目");
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
        order.setWorkOrderNo("WO-FILE-001");
        order.setProjectId(project.getId());
        order.setWorkTitle("附件上传工单");
        order.setWorkType("REPAIR");
        order.setWorkLocation("A平台");
        order.setWorkContent("附件上传测试");
        order.setMaintainerId(maintainerId);
        order.setStatus("IN_PROGRESS");
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
        assignment.setAssigneeId(maintainerId);
        assignment.setAssignmentRole("MAINTAINER");
        assignment.setAssignmentStatus("ACCEPTED");
        assignment.setAssignedAt(LocalDateTime.now());
        assignment.setVersion(1);
        assignment.setSyncStatus("SYNCED");
        assignment.setCreatedAt(LocalDateTime.now());
        assignment.setUpdatedAt(LocalDateTime.now());
        assignment.setDeletedFlag(0);
        workOrderAssignmentMapper.insert(assignment);
    }

    @Test
    void uploadStoresMetadataOnlyAndDoesNotExposeNakedPath() throws Exception {
        String token = login("file_worker", "MOBILE");
        String body = uploadFile(token);
        JsonNode data = objectMapper.readTree(body).path("data");

        assertThat(data.path("fileId").asText()).startsWith("FILE-");
        assertThat(data.path("previewUrl").asText()).startsWith("/api/files/");
        assertThat(data.has("filePath")).isFalse();

        FileStorage storage = fileStorageMapper.selectByFileId(data.path("fileId").asText());
        assertThat(storage).isNotNull();
        assertThat(storage.getFilePath()).isNotBlank();
        assertThat(storage.getStorageType()).isEqualTo("LOCAL");
        assertThat(storage.getFileHash()).hasSize(64);
    }

    @Test
    void downloadRequiresWorkOrderFileAuthorization() throws Exception {
        String ownerToken = login("file_worker", "MOBILE");
        String otherToken = login("file_other", "MOBILE");
        String fileId = objectMapper.readTree(uploadFile(ownerToken)).at("/data/fileId").asText();
        bindAttachment(ownerToken, fileId);

        mockMvc.perform(get("/api/files/{fileId}/download", fileId)
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(content().bytes("offshore-photo-content".getBytes()));

        mockMvc.perform(get("/api/files/{fileId}/download", fileId)
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void mobileCanBindWorkOrderAttachmentAndAdminCanListIt() throws Exception {
        String mobileToken = login("file_worker", "MOBILE");
        String adminToken = login("file_admin", "PC");
        String fileId = objectMapper.readTree(uploadFile(mobileToken)).at("/data/fileId").asText();

        bindAttachment(mobileToken, fileId)
                .andExpect(jsonPath("$.data.fileId").value(fileId))
                .andExpect(jsonPath("$.data.watermarkWorkOrderNo").value("WO-FILE-001"))
                .andExpect(jsonPath("$.data.previewUrl").value("/api/files/" + fileId + "/preview"));

        assertThat(workOrderAttachmentMapper.selectByWorkOrderId(workOrderId)).hasSize(1);

        mockMvc.perform(get("/api/mobile/work-orders/{workOrderId}/attachments", workOrderId)
                        .header("Authorization", "Bearer " + mobileToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].fileId").value(fileId));

        mockMvc.perform(get("/api/admin/work-orders/{workOrderId}/attachments", workOrderId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].fileId").value(fileId));

        assertThat(operationLogMapper.selectAll())
                .extracting(OperationLog::getOperationType)
                .contains("UPLOAD_FILE", "BIND_WORK_ORDER_ATTACHMENT");
    }

    private String uploadFile(String token) throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "site-photo.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "offshore-photo-content".getBytes());
        return mockMvc.perform(multipart("/api/files/upload")
                        .file(file)
                        .param("fileType", "PHOTO")
                        .param("workOrderId", String.valueOf(workOrderId))
                        .param("localId", "local-file-001")
                        .param("deviceId", "android-file-device")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.fileId").isString())
                .andExpect(jsonPath("$.data.fileHash").isString())
                .andExpect(jsonPath("$.data.downloadUrl").isString())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    private org.springframework.test.web.servlet.ResultActions bindAttachment(String token, String fileId) throws Exception {
        AttachmentBindRequest request = new AttachmentBindRequest();
        request.setLocalId("local-att-001");
        request.setFileId(fileId);
        request.setAttachmentType("PHOTO");
        request.setAttachmentName("现场照片");
        request.setAttachmentDesc("带水印的现场施工照片");
        request.setBusinessScene("WORK_RECORD");
        request.setCaptureTime(LocalDateTime.of(2026, 6, 5, 11, 0));
        request.setWatermarkFlag(1);
        request.setWatermarkText("WO-FILE-001 现场维修工 2026-06-05 11:00:00");
        request.setDeviceId("android-file-device");

        return mockMvc.perform(post("/api/mobile/work-orders/{workOrderId}/attachments", workOrderId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
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
                .andReturn()
                .getResponse()
                .getContentAsString();
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
