package com.offshore.platform.mapper;

import com.offshore.platform.entity.SysRole;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * sys_role 基础Mapper。
 */
public interface SysRoleMapper {
    int insert(SysRole sysRole);

    int updateById(SysRole sysRole);

    SysRole selectById(Long id);

    List<SysRole> selectByUserId(@Param("userId") Long userId);

    List<SysRole> selectAll();

    int softDeleteById(Long id);
}
