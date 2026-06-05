package com.offshore.platform;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.offshore.platform.dto.admin.AdminUserCreateRequest;
import com.offshore.platform.dto.admin.AdminUserUpdateRequest;
import com.offshore.platform.dto.admin.PermissionIdsRequest;
import com.offshore.platform.dto.admin.RoleIdsRequest;
import com.offshore.platform.dto.auth.LoginRequest;
import com.offshore.platform.entity.OperationLog;
import com.offshore.platform.entity.SysPermission;
import com.offshore.platform.entity.SysRole;
import com.offshore.platform.entity.SysUser;
import com.offshore.platform.entity.SysUserRole;
import com.offshore.platform.mapper.OperationLogMapper;
import com.offshore.platform.mapper.SysPermissionMapper;
import com.offshore.platform.mapper.SysRoleMapper;
import com.offshore.platform.mapper.SysRolePermissionMapper;
import com.offshore.platform.mapper.SysUserMapper;
import com.offshore.platform.mapper.SysUserRoleMapper;
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
class AdminSystemControllerTest {
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

    private Long adminUserId;
    private Long managerUserId;
    private Long adminRoleId;
    private Long managerRoleId;
    private Long permissionId;

    @BeforeEach
    void setUp() {
        SysRole adminRole = insertRole("SYSTEM_ADMIN", "系统管理员", "ALL");
        SysRole managerRole = insertRole("PROJECT_MANAGER", "项目经理", "PROJECT");
        adminRoleId = adminRole.getId();
        managerRoleId = managerRole.getId();

        SysPermission permission = new SysPermission();
        permission.setPermissionCode("ADMIN_USER_MANAGE");
        permission.setPermissionName("用户管理");
        permission.setPermissionType("API");
        permission.setPlatform("PC");
        permission.setApiMethod("GET");
        permission.setApiPath("/api/admin/users");
        permission.setSortOrder(1);
        permission.setVisibleFlag(0);
        permission.setStatus("ACTIVE");
        permission.setCreatedAt(LocalDateTime.now());
        permission.setUpdatedAt(LocalDateTime.now());
        permission.setDeletedFlag(0);
        sysPermissionMapper.insert(permission);
        permissionId = permission.getId();

        SysUser adminUser = insertUser("admin_user", "系统管理员", "13920000000");
        SysUser managerUser = insertUser("manager_user", "项目经理", "13920000001");
        adminUserId = adminUser.getId();
        managerUserId = managerUser.getId();
        insertUserRole(adminUserId, adminRoleId);
        insertUserRole(managerUserId, managerRoleId);
    }

    @Test
    void systemAdminCanManageUsersRolesAndPermissions() throws Exception {
        String token = login("admin_user");

        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + token)
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.records[0].passwordHash").doesNotExist());

        AdminUserCreateRequest createRequest = new AdminUserCreateRequest();
        createRequest.setUsername("new_admin_user");
        createRequest.setPassword("abc123456");
        createRequest.setRealName("新增用户");
        createRequest.setPhone("13920000002");
        createRequest.setPcEnabled(1);
        createRequest.setMobileEnabled(1);

        String createBody = mockMvc.perform(post("/api/admin/users")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("new_admin_user"))
                .andExpect(jsonPath("$.data.passwordHash").doesNotExist())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long newUserId = objectMapper.readTree(createBody).at("/data/id").asLong();
        SysUser stored = sysUserMapper.selectById(newUserId);
        assertThat(stored.getPasswordHash()).startsWith("{pbkdf2}");
        assertThat(stored.getPasswordHash()).isNotEqualTo("abc123456");

        AdminUserUpdateRequest updateRequest = new AdminUserUpdateRequest();
        updateRequest.setRealName("新增用户-更新");
        mockMvc.perform(put("/api/admin/users/{id}", newUserId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.realName").value("新增用户-更新"));

        RoleIdsRequest roleIdsRequest = new RoleIdsRequest();
        roleIdsRequest.setRoleIds(List.of(managerRoleId));
        mockMvc.perform(put("/api/admin/users/{id}/roles", newUserId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleIdsRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(get("/api/admin/users/{id}", newUserId).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roleCodes[0]").value("PROJECT_MANAGER"));

        PermissionIdsRequest permissionIdsRequest = new PermissionIdsRequest();
        permissionIdsRequest.setPermissionIds(List.of(permissionId));
        mockMvc.perform(put("/api/admin/roles/{id}/permissions", managerRoleId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(permissionIdsRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        assertThat(sysRolePermissionMapper.selectByRoleId(managerRoleId)).hasSize(1);

        mockMvc.perform(get("/api/admin/roles").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].roleCode").isNotEmpty());

        mockMvc.perform(get("/api/admin/permissions").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].permissionCode").isNotEmpty());

        mockMvc.perform(delete("/api/admin/users/{id}", newUserId).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        assertThat(sysUserMapper.selectById(newUserId)).isNull();

        assertThat(operationLogMapper.selectAll())
                .extracting(OperationLog::getOperationType)
                .contains("CREATE_USER", "UPDATE_USER", "ASSIGN_USER_ROLE", "ASSIGN_ROLE_PERMISSION", "DELETE_USER");
    }

    @Test
    void nonSystemAdminCannotAccessUserManagement() throws Exception {
        String token = login("manager_user");

        mockMvc.perform(get("/api/admin/users").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message", containsString("无访问权限")));
    }

    @Test
    void deletedUsersAreFilteredFromList() throws Exception {
        String token = login("admin_user");
        sysUserMapper.softDeleteById(managerUserId);

        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + token)
                        .param("keyword", "manager_user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(0))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.records[*].username", not(containsString("manager_user"))));
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
        JsonNode json = objectMapper.readTree(body);
        return json.at("/data/token").asText();
    }

    private SysUser insertUser(String username, String realName, String phone) {
        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPasswordHash("{noop}123456");
        user.setRealName(realName);
        user.setPhone(phone);
        user.setAccountStatus("ACTIVE");
        user.setPcEnabled(1);
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
