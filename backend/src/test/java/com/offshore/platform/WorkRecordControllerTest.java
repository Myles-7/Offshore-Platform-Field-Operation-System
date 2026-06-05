package com.offshore.platform;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.offshore.platform.dto.auth.LoginRequest;
import com.offshore.platform.dto.mobile.MobileCheckItemBatchRequest;
import com.offshore.platform.dto.mobile.MobileCheckItemRequest;
import com.offshore.platform.dto.mobile.MobileWorkRecordRequest;
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
import com.offshore.platform.mapper.WorkOrderCheckItemMapper;
import com.offshore.platform.mapper.WorkOrderMapper;
import com.offshore.platform.mapper.WorkOrderRecordMapper;
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
class WorkRecordControllerTest {
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
    private WorkOrderRecordMapper workOrderRecordMapper;

    @Autowired
    private WorkOrderCheckItemMapper workOrderCheckItemMapper;

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
        SysUser admin = insertUser("record_admin", "记录管理员", "13950000000", 1, 0);
        SysUser maintainer = insertUser("record_worker", "施工人员", "13950000001", 0, 1);
        SysUser other = insertUser("record_other", "其他施工人员", "13950000002", 0, 1);
        adminId = admin.getId();
        maintainerId = maintainer.getId();
        otherMaintainerId = other.getId();
        insertUserRole(adminId, adminRole.getId());
        insertUserRole(maintainerId, maintainerRole.getId());
        insertUserRole(otherMaintainerId, maintainerRole.getId());

        ProjectInfo project = new ProjectInfo();
        project.setProjectCode("PRJ-REC");
        project.setProjectName("施工记录项目");
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
        order.setWorkOrderNo("WO-REC-001");
        order.setProjectId(project.getId());
        order.setWorkTitle("施工记录工单");
        order.setWorkType("REPAIR");
        order.setWorkLocation("A平台");
        order.setWorkContent("施工记录测试");
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
    void mobileCanCreateAndUpdateWorkRecordWithVersionCheck() throws Exception {
        String token = login("record_worker", "MOBILE");
        Long recordId = createRecord(token);

        assertThat(workOrderRecordMapper.selectById(recordId).getProjectId()).isNotNull();
        assertThat(workOrderRecordMapper.selectById(recordId).getServerId()).isEqualTo(recordId);

        MobileWorkRecordRequest update = baseRecordRequest();
        update.setVersion(1);
        update.setConstructionDesc("更新后的施工描述");
        mockMvc.perform(put("/api/mobile/work-orders/{workOrderId}/records/{recordId}", workOrderId, recordId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.version").value(2))
                .andExpect(jsonPath("$.data.constructionDesc").value("更新后的施工描述"));

        MobileWorkRecordRequest stale = baseRecordRequest();
        stale.setVersion(1);
        mockMvc.perform(put("/api/mobile/work-orders/{workOrderId}/records/{recordId}", workOrderId, recordId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(stale)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40001));
        assertThat(workOrderRecordMapper.selectById(recordId).getSyncStatus()).isEqualTo("CONFLICT");
    }

    @Test
    void mobileCannotAccessOtherMaintainersWorkRecords() throws Exception {
        String otherToken = login("record_other", "MOBILE");

        mockMvc.perform(get("/api/mobile/work-orders/{workOrderId}/records", workOrderId)
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));

        mockMvc.perform(post("/api/mobile/work-orders/{workOrderId}/records", workOrderId)
                        .header("Authorization", "Bearer " + otherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(baseRecordRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void adminCanViewTimelineAndConfirmRecord() throws Exception {
        String mobileToken = login("record_worker", "MOBILE");
        String adminToken = login("record_admin", "PC");
        Long recordId = createRecord(mobileToken);

        MobileCheckItemBatchRequest batch = new MobileCheckItemBatchRequest();
        MobileCheckItemRequest item = new MobileCheckItemRequest();
        item.setItemCode("CHK-001");
        item.setItemName("防腐层外观");
        item.setCheckResult("NORMAL");
        item.setCheckValue("合格");
        item.setAbnormalFlag(0);
        batch.setItems(List.of(item));

        mockMvc.perform(post("/api/mobile/work-records/{recordId}/check-items", recordId)
                        .header("Authorization", "Bearer " + mobileToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(batch)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].serverId").isNumber());
        assertThat(workOrderCheckItemMapper.selectByRecordId(recordId)).hasSize(1);

        mockMvc.perform(get("/api/admin/work-orders/{workOrderId}/records", workOrderId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].checkItems.length()").value(1));

        mockMvc.perform(get("/api/admin/work-records/{recordId}/timeline", recordId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].eventType").value("RECORD"))
                .andExpect(jsonPath("$.data[1].eventType").value("CHECK_ITEM"));

        mockMvc.perform(post("/api/admin/work-records/{recordId}/confirm", recordId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.recordStatus").value("CONFIRMED"));

        assertThat(operationLogMapper.selectAll())
                .extracting(OperationLog::getOperationType)
                .contains("CREATE_WORK_RECORD", "CREATE_WORK_RECORD_CHECK_ITEMS", "CONFIRM_WORK_RECORD");
    }

    private Long createRecord(String token) throws Exception {
        String body = mockMvc.perform(post("/api/mobile/work-orders/{workOrderId}/records", workOrderId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(baseRecordRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.localId").value("local-rec-001"))
                .andExpect(jsonPath("$.data.serverId").isNumber())
                .andExpect(jsonPath("$.data.version").value(1))
                .andExpect(jsonPath("$.data.syncStatus").value("SYNCED"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(body).at("/data/id").asLong();
    }

    private MobileWorkRecordRequest baseRecordRequest() {
        MobileWorkRecordRequest request = new MobileWorkRecordRequest();
        request.setLocalId("local-rec-001");
        request.setRecordType("DAILY");
        request.setConstructionTime(LocalDateTime.of(2026, 6, 5, 10, 0));
        request.setConstructionDesc("完成现场打磨和第一遍涂装");
        request.setSiteCondition("海风较大");
        request.setAbnormalFlag(1);
        request.setAbnormalDesc("局部锈蚀较重");
        request.setWeather("CLOUDY");
        request.setLocationName("A平台甲板");
        request.setDeviceId("android-test-device");
        return request;
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
