package com.offshore.platform;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.offshore.platform.dto.admin.ProjectRequest;
import com.offshore.platform.dto.admin.WorkOrderAssignRequest;
import com.offshore.platform.dto.admin.WorkOrderFromTemplateRequest;
import com.offshore.platform.dto.admin.WorkOrderRequest;
import com.offshore.platform.dto.admin.WorkOrderStatusRequest;
import com.offshore.platform.dto.admin.WorkOrderTemplateRequest;
import com.offshore.platform.dto.auth.LoginRequest;
import com.offshore.platform.entity.OperationLog;
import com.offshore.platform.entity.SysRole;
import com.offshore.platform.entity.SysUser;
import com.offshore.platform.entity.SysUserRole;
import com.offshore.platform.mapper.OperationLogMapper;
import com.offshore.platform.mapper.SysRoleMapper;
import com.offshore.platform.mapper.SysUserMapper;
import com.offshore.platform.mapper.SysUserRoleMapper;
import com.offshore.platform.mapper.WorkOrderAssignmentMapper;
import com.offshore.platform.mapper.WorkOrderMapper;
import com.offshore.platform.mapper.WorkOrderStatusLogMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
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
class AdminWorkOrderControllerTest {
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
    private WorkOrderMapper workOrderMapper;

    @Autowired
    private WorkOrderAssignmentMapper workOrderAssignmentMapper;

    @Autowired
    private WorkOrderStatusLogMapper workOrderStatusLogMapper;

    @Autowired
    private OperationLogMapper operationLogMapper;

    private Long adminUserId;
    private Long maintainerUserId;

    @BeforeEach
    void setUp() {
        SysRole adminRole = insertRole("SYSTEM_ADMIN", "系统管理员", "ALL");
        SysRole maintainerRole = insertRole("MAINTAINER", "维修工", "SELF");
        SysUser admin = insertUser("wo_admin", "工单管理员", "13930000000", 1, 1);
        SysUser maintainer = insertUser("wo_maintainer", "维修工", "13930000001", 0, 1);
        adminUserId = admin.getId();
        maintainerUserId = maintainer.getId();
        insertUserRole(adminUserId, adminRole.getId());
        insertUserRole(maintainerUserId, maintainerRole.getId());
    }

    @Test
    void createAssignAndChangeStatusWriteWorkOrderFlow() throws Exception {
        String token = login("wo_admin");
        Long projectId = createProject(token);

        WorkOrderRequest create = new WorkOrderRequest();
        create.projectId = projectId;
        create.workTitle = "海管防腐修复";
        create.workType = "ANTICORROSION";
        create.workLocation = "A平台甲板";
        create.workContent = "打磨、补漆、防腐层修复";
        create.priority = "HIGH";
        create.plannedStartTime = LocalDateTime.of(2026, 6, 6, 8, 0);
        create.plannedEndTime = LocalDateTime.of(2026, 6, 6, 18, 0);

        String createBody = mockMvc.perform(post("/api/admin/work-orders")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(create)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("PENDING_ASSIGN"))
                .andExpect(jsonPath("$.data.priority").value("HIGH"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long workOrderId = objectMapper.readTree(createBody).at("/data/id").asLong();

        WorkOrderAssignRequest assign = new WorkOrderAssignRequest();
        assign.maintainerId = maintainerUserId;
        assign.leaderId = adminUserId;
        assign.remark = "安排维修工现场处理";
        mockMvc.perform(post("/api/admin/work-orders/{id}/assign", workOrderId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assign)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ASSIGNED"))
                .andExpect(jsonPath("$.data.maintainerId").value(maintainerUserId));

        assertThat(workOrderAssignmentMapper.selectByWorkOrderId(workOrderId)).hasSize(1);

        WorkOrderStatusRequest start = new WorkOrderStatusRequest();
        start.status = "IN_PROGRESS";
        start.operationDesc = "现场开始施工";
        mockMvc.perform(post("/api/admin/work-orders/{id}/status", workOrderId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(start)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"));

        mockMvc.perform(get("/api/admin/work-orders/{id}/status-flow", workOrderId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0].operationType").value("CREATE"))
                .andExpect(jsonPath("$.data[1].operationType").value("ASSIGN"))
                .andExpect(jsonPath("$.data[2].operationType").value("START"));

        mockMvc.perform(get("/api/admin/work-orders/{id}", workOrderId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.workOrder.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.data.project.projectCode").value("PRJ-WO-TEST"))
                .andExpect(jsonPath("$.data.assignments.length()").value(1))
                .andExpect(jsonPath("$.data.syncSummary.syncStatus").value("SYNCED"));

        assertThat(workOrderMapper.selectById(workOrderId).getStatus()).isEqualTo("IN_PROGRESS");
        assertThat(workOrderStatusLogMapper.selectByWorkOrderId(workOrderId)).hasSize(3);
        assertThat(operationLogMapper.selectAll())
                .extracting(OperationLog::getOperationType)
                .contains("CREATE_PROJECT", "CREATE_WORK_ORDER", "ASSIGN_WORK_ORDER", "CHANGE_WORK_ORDER_STATUS");
        mockMvc.perform(get("/api/admin/work-orders/{workOrderId}/audit-trail", workOrderId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.action == 'ASSIGN_WORK_ORDER')]").exists());
    }

    @Test
    void templateCanCreateWorkOrderFromTemplate() throws Exception {
        String token = login("wo_admin");
        Long projectId = createProject(token);

        WorkOrderTemplateRequest template = new WorkOrderTemplateRequest();
        template.templateCode = "TPL-ANTICORROSION";
        template.templateName = "防腐修复模板";
        template.workType = "ANTICORROSION";
        template.defaultPriority = "NORMAL";
        template.defaultWorkContent = "标准防腐修复流程";
        template.defaultMaterialDesc = "底漆、面漆、砂纸";

        String templateBody = mockMvc.perform(post("/api/admin/work-order-templates")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(template)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.templateCode").value("TPL-ANTICORROSION"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long templateId = objectMapper.readTree(templateBody).at("/data/id").asLong();

        WorkOrderFromTemplateRequest create = new WorkOrderFromTemplateRequest();
        create.projectId = projectId;
        create.workTitle = "模板生成工单";
        create.workLocation = "B平台管廊";
        create.maintainerId = maintainerUserId;

        mockMvc.perform(post("/api/admin/work-orders/from-template/{templateId}", templateId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(create)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.templateId").value(templateId))
                .andExpect(jsonPath("$.data.workType").value("ANTICORROSION"))
                .andExpect(jsonPath("$.data.workContent").value("标准防腐修复流程"))
                .andExpect(jsonPath("$.data.status").value("PENDING_ASSIGN"));

        mockMvc.perform(get("/api/admin/work-order-templates").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].templateCode").value("TPL-ANTICORROSION"));
    }

    private Long createProject(String token) throws Exception {
        ProjectRequest project = new ProjectRequest();
        project.projectCode = "PRJ-WO-TEST";
        project.projectName = "工单测试项目";
        project.platformName = "A平台";
        project.projectManagerId = adminUserId;
        project.projectLocation = "南海";
        project.startDate = LocalDate.of(2026, 6, 1);
        project.projectStatus = "ACTIVE";

        String body = mockMvc.perform(post("/api/admin/projects")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(project)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.projectCode").value("PRJ-WO-TEST"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode json = objectMapper.readTree(body);
        return json.at("/data/id").asLong();
    }

    private String login(String username) throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setLoginName(username);
        loginRequest.setPassword("123456");
        loginRequest.setPlatform("PC");
        String body = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
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

    private SysRole insertRole(String roleCode, String roleName, String dataScope) {
        SysRole role = new SysRole();
        role.setRoleCode(roleCode);
        role.setRoleName(roleName);
        role.setRoleType("SYSTEM_ADMIN".equals(roleCode) ? "SYSTEM" : "BUSINESS");
        role.setDataScope(dataScope);
        role.setPcEnabled(1);
        role.setMobileEnabled(1);
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
