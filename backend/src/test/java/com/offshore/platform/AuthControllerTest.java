package com.offshore.platform;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.offshore.platform.dto.auth.LoginRequest;
import com.offshore.platform.entity.OperationLog;
import com.offshore.platform.entity.SysPermission;
import com.offshore.platform.entity.SysRole;
import com.offshore.platform.entity.SysRolePermission;
import com.offshore.platform.entity.SysUser;
import com.offshore.platform.entity.SysUserRole;
import com.offshore.platform.mapper.OperationLogMapper;
import com.offshore.platform.mapper.SysPermissionMapper;
import com.offshore.platform.mapper.SysRoleMapper;
import com.offshore.platform.mapper.SysRolePermissionMapper;
import com.offshore.platform.mapper.SysUserMapper;
import com.offshore.platform.mapper.SysUserRoleMapper;
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
class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private SysPermissionMapper sysPermissionMapper;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Autowired
    private SysRolePermissionMapper sysRolePermissionMapper;

    @Autowired
    private OperationLogMapper operationLogMapper;

    private Long userId;

    @BeforeEach
    void setUp() {
        SysUser user = new SysUser();
        user.setUsername("auth_admin");
        user.setPasswordHash("{noop}123456");
        user.setRealName("认证管理员");
        user.setPhone("13910000000");
        user.setAccountStatus("ACTIVE");
        user.setPcEnabled(1);
        user.setMobileEnabled(1);
        user.setPrimaryProjectId(1001L);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setDeletedFlag(0);
        sysUserMapper.insert(user);
        userId = user.getId();

        SysRole role = new SysRole();
        role.setRoleCode("SYSTEM_ADMIN");
        role.setRoleName("系统管理员");
        role.setRoleType("SYSTEM");
        role.setDataScope("ALL");
        role.setPcEnabled(1);
        role.setMobileEnabled(1);
        role.setSortOrder(1);
        role.setStatus("ACTIVE");
        role.setCreatedAt(LocalDateTime.now());
        role.setUpdatedAt(LocalDateTime.now());
        role.setDeletedFlag(0);
        sysRoleMapper.insert(role);

        SysPermission permission = new SysPermission();
        permission.setPermissionCode("AUTH_CURRENT");
        permission.setPermissionName("查看当前用户");
        permission.setPermissionType("API");
        permission.setPlatform("PC");
        permission.setApiMethod("GET");
        permission.setApiPath("/api/auth/current");
        permission.setSortOrder(1);
        permission.setVisibleFlag(1);
        permission.setStatus("ACTIVE");
        permission.setCreatedAt(LocalDateTime.now());
        permission.setUpdatedAt(LocalDateTime.now());
        permission.setDeletedFlag(0);
        sysPermissionMapper.insert(permission);

        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(user.getId());
        userRole.setRoleId(role.getId());
        userRole.setCreatedAt(LocalDateTime.now());
        userRole.setUpdatedAt(LocalDateTime.now());
        userRole.setDeletedFlag(0);
        sysUserRoleMapper.insert(userRole);

        SysRolePermission rolePermission = new SysRolePermission();
        rolePermission.setRoleId(role.getId());
        rolePermission.setPermissionId(permission.getId());
        rolePermission.setCreatedAt(LocalDateTime.now());
        rolePermission.setUpdatedAt(LocalDateTime.now());
        rolePermission.setDeletedFlag(0);
        sysRolePermissionMapper.insert(rolePermission);
    }

    @Test
    void loginCurrentAndLogoutUseJwtCurrentUserContextAndOperationLog() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setLoginName("auth_admin");
        request.setPassword("123456");
        request.setPlatform("PC");

        String loginBody = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-Forwarded-For", "10.1.2.3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.userId").value(userId))
                .andExpect(jsonPath("$.data.username").value("auth_admin"))
                .andExpect(jsonPath("$.data.realName").value("认证管理员"))
                .andExpect(jsonPath("$.data.roleCodes[0]").value("SYSTEM_ADMIN"))
                .andExpect(jsonPath("$.data.permissionCodes[0]").value("AUTH_CURRENT"))
                .andExpect(jsonPath("$.data.dataScope").value("ALL"))
                .andExpect(jsonPath("$.data.primaryProjectId").value(1001))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode loginJson = objectMapper.readTree(loginBody);
        String token = loginJson.at("/data/token").asText();
        assertThat(sysUserMapper.selectById(userId).getLastLoginIp()).isEqualTo("10.1.2.3");

        mockMvc.perform(get("/api/auth/current").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.userId").value(userId))
                .andExpect(jsonPath("$.data.username").value("auth_admin"))
                .andExpect(jsonPath("$.data.roleCodes[0]").value("SYSTEM_ADMIN"));

        mockMvc.perform(post("/api/auth/logout").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        assertThat(operationLogMapper.selectAll())
                .extracting(OperationLog::getOperationType)
                .contains("LOGIN", "LOGOUT");
    }

    @Test
    void currentRequiresBearerToken() throws Exception {
        mockMvc.perform(get("/api/auth/current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message", containsString("未登录")));
    }

    @Test
    void loginRejectsWrongPassword() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setLoginName("auth_admin");
        request.setPassword("wrong");
        request.setPlatform("PC");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(10001))
                .andExpect(jsonPath("$.message").value("用户名或密码错误"));
    }
}
