package com.offshore.platform.mapper;

import com.offshore.platform.entity.SysUserRole;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * sys_user_role 基础Mapper。
 */
public interface SysUserRoleMapper {
    int insert(SysUserRole sysUserRole);

    int updateById(SysUserRole sysUserRole);

    SysUserRole selectById(Long id);

    SysUserRole selectByUserIdAndRoleId(@Param("userId") Long userId, @Param("roleId") Long roleId);

    List<SysUserRole> selectByUserId(@Param("userId") Long userId);

    List<SysUserRole> selectAll();

    int softDeleteById(Long id);

    int softDeleteByUserId(@Param("userId") Long userId, @Param("updatedBy") Long updatedBy);
}
