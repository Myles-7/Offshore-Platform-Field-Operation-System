package com.offshore.platform.mapper;

import com.offshore.platform.entity.SysPermission;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * sys_permission 基础Mapper。
 */
public interface SysPermissionMapper {
    int insert(SysPermission sysPermission);

    int updateById(SysPermission sysPermission);

    SysPermission selectById(Long id);

    List<SysPermission> selectByUserId(@Param("userId") Long userId);

    List<SysPermission> selectAll();

    int softDeleteById(Long id);
}
