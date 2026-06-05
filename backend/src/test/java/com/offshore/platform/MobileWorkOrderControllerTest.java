package com.offshore.platform;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.offshore.platform.dto.auth.LoginRequest;
import com.offshore.platform.dto.mobile.MobileSubmitAcceptanceRequest;
import com.offshore.platform.entity.OperationLog;
import com.offshore.platform.entity.ProjectInfo;
import com.offshore.platform.entity.SysRole;
import com.offshore.platform.entity.SysUser;
import com.offshore.platform.entity.SysUserRole;
import com.offshore.platform.entity.WorkOrder;
import com.offshore.platform.entity.WorkOrderAssignment;
import com.offshore.platform.mapper.OperationLogMapper;
import com.offshore.platform.mapper.ProjectInfoMapper;
import com.offshore.platform.mapper.SysRoleMapper;
import com.offshore.platform.mapper.SysUserMapper;
import com.offshore.platform.mapper.SysUserRoleMapper;
import com.offshore.platform.mapper.WorkOrderAssignmentMapper;
import com.offshore.platform.mapper.WorkOrderMapper;
import com.offshore.platform.mapper.WorkOrderStatusLogMapper;
import java.time.LocalDateTime;
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
class MobileWorkOrderControllerTest {
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
    private WorkOrderStatusLogMapper workOrderStatusLogMapper;

    @Autowired
    private OperationLogMapper operationLogMapper;

    private Long maintainerId;
    private Long otherMaintainerId;
    private Long workOrderId;

    @BeforeEach
    void setUp() {
        SysRole maintainerRole = insertRole("MAINTAINER", "维修工", "SELF");
        SysUser maintainer = insertUser("mobile_worker", "移动维修工", "13940000001");
        SysUser other = insertUser("mobile_other", "其他维修工", "13940000002");
        maintainerId = maintainer.getId();
        otherMaintainerId = other.getId();
        insertUserRole(maintainerId, maintainerRole.getId());
        insertUserRole(otherMaintainerId, maintainerRole.getId());

        ProjectInfo project = new ProjectInfo();
        project.setProjectCode("PRJ-MOBILE");
        project.setProjectName("移动端项目");
        project.setProjectStatus("ACTIVE");
        project.setSortOrder(1);
        project.setVersion(1);
        project.setSyncStatus("SYNCED");
        project.setCreatedAt(LocalDateTime.now());
        project.setUpdatedAt(LocalDateTime.now());
        project.setDeletedFlag(0);
        projectInfoMapper.insert(project);

        WorkOrder order = new WorkOrder();
        order.setWorkOrderNo("WO-MOBILE-001");
        order.setProjectId(project.getId());
        order.setWorkTitle("移动端工单");
        order.setWorkType("REPAIR");
        order.setWorkLocation("A平台");
        order.setWorkContent("移动端可缓存的工单内容");
        order.setMaintainerId(maintainerId);
        order.setPlannedStartTime(LocalDateTime.of(2026, 6, 6, 8, 0));
        order.setPlannedEndTime(LocalDateTime.of(2026, 6, 6, 18, 0));
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
        workOrderId = order.getId();

        WorkOrderAssignment assignment = new WorkOrderAssignment();
        assignment.setWorkOrderId(workOrderId);
        assignment.setAssignerId(maintainerId);
        assignment.setAssigneeId(maintainerId);
        assignment.setAssignmentRole("MAINTAINER");
        assignment.setAssignmentStatus("ASSIGNED");
        assignment.setAssignedAt(LocalDateTime.now());
        assignment.setVersion(1);
        assignment.setSyncStatus("SYNCED");
        assignment.setCreatedAt(LocalDateTime.now());
        assignment.setUpdatedAt(LocalDateTime.now());
        assignment.setDeletedFlag(0);
        workOrderAssignmentMapper.insert(assignment);
    }

    @Test
    void mobileUserCanOnlyAccessOwnWorkOrders() throws Exception {
        String myToken = login("mobile_worker");
        String otherToken = login("mobile_other");

        mockMvc.perform(get("/api/mobile/work-orders").header("Authorization", "Bearer " + myToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(workOrderId))
                .andExpect(jsonPath("$.data[0].version").value(1))
                .andExpect(jsonPath("$.data[0].syncStatus").value("SYNCED"));

        mockMvc.perform(get("/api/mobile/work-orders").header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));

        mockMvc.perform(get("/api/mobile/work-orders/{id}", workOrderId).header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void illegalStatusTransitionIsRejected() throws Exception {
        String token = login("mobile_worker");
        MobileSubmitAcceptanceRequest request = new MobileSubmitAcceptanceRequest();
        request.setSubmitDesc("还没开始就提交");

        mockMvc.perform(post("/api/mobile/work-orders/{id}/submit-acceptance", workOrderId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(20001));
    }

    @Test
    void acceptStartAndSubmitAcceptanceWriteMobileLogs() throws Exception {
        String token = login("mobile_worker");

        mockMvc.perform(post("/api/mobile/work-orders/{id}/accept", workOrderId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("ASSIGNED"));

        mockMvc.perform(post("/api/mobile/work-orders/{id}/start", workOrderId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"));

        MobileSubmitAcceptanceRequest submit = new MobileSubmitAcceptanceRequest();
        submit.setSubmitDesc("施工完成，提交验收");
        mockMvc.perform(post("/api/mobile/work-orders/{id}/submit-acceptance", workOrderId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(submit)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PENDING_ACCEPTANCE"));

        mockMvc.perform(get("/api/mobile/work-orders/{id}/materials", workOrderId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(get("/api/mobile/work-orders/{id}/qualification-check", workOrderId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        assertThat(workOrderMapper.selectById(workOrderId).getStatus()).isEqualTo("PENDING_ACCEPTANCE");
        assertThat(workOrderStatusLogMapper.selectByWorkOrderId(workOrderId))
                .extracting(item -> item.getOperationType())
                .contains("ACCEPT", "START", "SUBMIT_ACCEPTANCE");
        assertThat(operationLogMapper.selectAll())
                .filteredOn(log -> "MOBILE".equals(log.getPlatform()))
                .extracting(OperationLog::getOperationType)
                .contains("ACCEPT_WORK_ORDER", "START_WORK_ORDER", "SUBMIT_ACCEPTANCE");
    }

    private String login(String username) throws Exception {
        LoginRequest request = new LoginRequest();
        request.setLoginName(username);
        request.setPassword("123456");
        request.setPlatform("MOBILE");
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

    private SysUser insertUser(String username, String realName, String phone) {
        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPasswordHash("{noop}123456");
        user.setRealName(realName);
        user.setPhone(phone);
        user.setAccountStatus("ACTIVE");
        user.setPcEnabled(0);
        user.setMobileEnabled(1);
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
        role.setRoleType("BUSINESS");
        role.setDataScope(dataScope);
        role.setPcEnabled(0);
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
