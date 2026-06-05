package com.offshore.platform.controller;

import com.offshore.platform.common.page.PageRequestDTO;
import com.offshore.platform.common.page.PageResult;
import com.offshore.platform.common.response.ApiResponse;
import com.offshore.platform.dto.admin.AdminUserCreateRequest;
import com.offshore.platform.dto.admin.AdminUserUpdateRequest;
import com.offshore.platform.dto.admin.PermissionIdsRequest;
import com.offshore.platform.dto.admin.RoleIdsRequest;
import com.offshore.platform.service.AdminSystemService;
import com.offshore.platform.vo.admin.AdminUserVO;
import com.offshore.platform.vo.admin.PermissionVO;
import com.offshore.platform.vo.admin.RoleVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "auth", description = "用户认证与权限")
@Validated
@RestController
@RequestMapping("/api/admin")
public class AdminSystemController {
    private final AdminSystemService adminSystemService;

    public AdminSystemController(AdminSystemService adminSystemService) {
        this.adminSystemService = adminSystemService;
    }

    @Operation(summary = "PC后台用户列表")
    @GetMapping("/users")
    public ApiResponse<PageResult<AdminUserVO>> listUsers(@Valid PageRequestDTO pageRequest) {
        return ApiResponse.success(adminSystemService.listUsers(pageRequest));
    }

    @Operation(summary = "PC后台用户详情")
    @GetMapping("/users/{id}")
    public ApiResponse<AdminUserVO> getUser(@PathVariable Long id) {
        return ApiResponse.success(adminSystemService.getUser(id));
    }

    @Operation(summary = "PC后台新增用户")
    @PostMapping("/users")
    public ApiResponse<AdminUserVO> createUser(@Valid @RequestBody AdminUserCreateRequest request,
            HttpServletRequest servletRequest) {
        return ApiResponse.success(adminSystemService.createUser(request, servletRequest));
    }

    @Operation(summary = "PC后台更新用户")
    @PutMapping("/users/{id}")
    public ApiResponse<AdminUserVO> updateUser(@PathVariable Long id,
            @Valid @RequestBody AdminUserUpdateRequest request, HttpServletRequest servletRequest) {
        return ApiResponse.success(adminSystemService.updateUser(id, request, servletRequest));
    }

    @Operation(summary = "PC后台删除用户")
    @DeleteMapping("/users/{id}")
    public ApiResponse<Void> deleteUser(@PathVariable Long id, HttpServletRequest servletRequest) {
        adminSystemService.deleteUser(id, servletRequest);
        return ApiResponse.success();
    }

    @Operation(summary = "PC后台角色列表")
    @GetMapping("/roles")
    public ApiResponse<List<RoleVO>> listRoles() {
        return ApiResponse.success(adminSystemService.listRoles());
    }

    @Operation(summary = "PC后台权限列表")
    @GetMapping("/permissions")
    public ApiResponse<List<PermissionVO>> listPermissions() {
        return ApiResponse.success(adminSystemService.listPermissions());
    }

    @Operation(summary = "PC后台分配用户角色")
    @PutMapping("/users/{id}/roles")
    public ApiResponse<Void> assignUserRoles(@PathVariable Long id, @Valid @RequestBody RoleIdsRequest request,
            HttpServletRequest servletRequest) {
        adminSystemService.assignUserRoles(id, request, servletRequest);
        return ApiResponse.success();
    }

    @Operation(summary = "PC后台分配角色权限")
    @PutMapping("/roles/{id}/permissions")
    public ApiResponse<Void> assignRolePermissions(@PathVariable Long id,
            @Valid @RequestBody PermissionIdsRequest request, HttpServletRequest servletRequest) {
        adminSystemService.assignRolePermissions(id, request, servletRequest);
        return ApiResponse.success();
    }
}
