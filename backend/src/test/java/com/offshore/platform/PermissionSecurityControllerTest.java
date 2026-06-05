package com.offshore.platform;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.offshore.platform.dto.auth.LoginRequest;
import com.offshore.platform.entity.DeviceInfo;
import com.offshore.platform.entity.SysRole;
import com.offshore.platform.entity.SysUser;
import com.offshore.platform.entity.SysUserRole;
import com.offshore.platform.mapper.DeviceInfoMapper;
import com.offshore.platform.mapper.SysRoleMapper;
import com.offshore.platform.mapper.SysUserMapper;
import com.offshore.platform.mapper.SysUserRoleMapper;
import java.time.LocalDateTime;
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
class PermissionSecurityControllerTest {
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
    private DeviceInfoMapper deviceInfoMapper;

    private Long workerUserId;
    private Long otherWorkerUserId;

    @BeforeEach
    void setUp() {
        SysRole workerRole = insertRole("MAINTAINER", "Maintainer", "SELF", 0, 1);
        SysRole projectManagerRole = insertRole("PROJECT_MANAGER", "Project Manager", "PROJECT", 1, 0);
        SysRole materialRole = insertRole("MATERIAL_MANAGER", "Material Manager", "CUSTOM", 1, 0);
        SysRole qualificationRole = insertRole("QUALIFICATION_MANAGER", "Qualification Manager", "CUSTOM", 1, 0);
        SysRole businessRole = insertRole("BUSINESS_USER", "Business User", "CUSTOM", 1, 0);

        workerUserId = insertUser("perm_worker", "Worker", "13991000001", 0, 1).getId();
        otherWorkerUserId = insertUser("perm_other_worker", "Other Worker", "13991000002", 0, 1).getId();
        Long managerUserId = insertUser("perm_manager", "Manager", "13991000003", 1, 0).getId();
        Long materialUserId = insertUser("perm_material", "Material", "13991000004", 1, 0).getId();
        Long qualificationUserId = insertUser("perm_qualification", "Qualification", "13991000005", 1, 0).getId();
        Long businessUserId = insertUser("perm_business", "Business", "13991000006", 1, 0).getId();

        insertUserRole(workerUserId, workerRole.getId());
        insertUserRole(otherWorkerUserId, workerRole.getId());
        insertUserRole(managerUserId, projectManagerRole.getId());
        insertUserRole(materialUserId, materialRole.getId());
        insertUserRole(qualificationUserId, qualificationRole.getId());
        insertUserRole(businessUserId, businessRole.getId());

        insertDevice("android-perm-owner", workerUserId);
    }

    @Test
    void anonymousLoginAndHealthAreOpenButAdminIsProtected() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setLoginName("perm_manager");
        request.setPassword("123456");
        request.setPlatform("PC");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void endpointClientSwitchesAreEnforced() throws Exception {
        String workerToken = login("perm_worker", "MOBILE");
        String managerToken = login("perm_manager", "PC");

        mockMvc.perform(get("/api/admin/projects").header("Authorization", "Bearer " + workerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));

        mockMvc.perform(get("/api/mobile/work-orders").header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void materialManagerIsLimitedToMaterialAdminApis() throws Exception {
        String token = login("perm_material", "PC");

        mockMvc.perform(get("/api/admin/materials").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(get("/api/admin/employees").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void qualificationManagerIsLimitedToQualificationAdminApis() throws Exception {
        String token = login("perm_qualification", "PC");

        mockMvc.perform(get("/api/admin/employees").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(get("/api/admin/materials").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void businessUserIsLimitedToDashboardAndReports() throws Exception {
        String token = login("perm_business", "PC");

        mockMvc.perform(get("/api/admin/dashboard/overview").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(get("/api/admin/users").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void syncRequiresRegisteredCurrentUserDevice() throws Exception {
        String ownerToken = login("perm_worker", "MOBILE");
        String otherToken = login("perm_other_worker", "MOBILE");

        mockMvc.perform(get("/api/sync/tasks").header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40001));

        mockMvc.perform(get("/api/sync/tasks")
                        .header("Authorization", "Bearer " + ownerToken)
                        .header("X-Device-Id", "android-perm-owner"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(get("/api/sync/tasks")
                        .header("Authorization", "Bearer " + otherToken)
                        .header("X-Device-Id", "android-perm-owner"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void syncRegisterAllowsNewDeviceButRejectsAnotherUsersExistingDevice() throws Exception {
        String ownerToken = login("perm_worker", "MOBILE");
        String otherToken = login("perm_other_worker", "MOBILE");

        mockMvc.perform(post("/api/sync/device/register")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "deviceId", "android-perm-new",
                                "deviceName", "Android New",
                                "platform", "ANDROID"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(post("/api/sync/device/register")
                        .header("Authorization", "Bearer " + otherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "deviceId", "android-perm-owner",
                                "deviceName", "Android Owner",
                                "platform", "ANDROID"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
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
        role.setRoleType("BUSINESS");
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

    private void insertDevice(String deviceId, Long userId) {
        DeviceInfo device = new DeviceInfo();
        device.setDeviceId(deviceId);
        device.setUserId(userId);
        device.setDeviceName("Android Owner");
        device.setPlatform("ANDROID");
        device.setDeviceStatus("ACTIVE");
        device.setSyncEnabled(1);
        device.setLastHeartbeatTime(LocalDateTime.now());
        device.setCreatedAt(LocalDateTime.now());
        device.setUpdatedAt(LocalDateTime.now());
        device.setDeletedFlag(0);
        deviceInfoMapper.insert(device);
    }
}
