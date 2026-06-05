package com.offshore.platform.mapper;

import com.offshore.platform.entity.SysRolePermission;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * sys_role_permission 基础Mapper。
 */
public interface SysRolePermissionMapper {
    int insert(SysRolePermission sysRolePermission);

    int updateById(SysRolePermission sysRolePermission);

    SysRolePermission selectById(Long id);

    SysRolePermission selectByRoleIdAndPermissionId(@Param("roleId") Long roleId, @Param("permissionId") Long permissionId);

    List<SysRolePermission> selectByRoleId(@Param("roleId") Long roleId);

    List<SysRolePermission> selectAll();

    int softDeleteById(Long id);

    int softDeleteByRoleId(@Param("roleId") Long roleId, @Param("updatedBy") Long updatedBy);
}
