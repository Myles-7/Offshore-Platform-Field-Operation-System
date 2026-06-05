package com.offshore.platform.mapper;

import com.offshore.platform.entity.SysUser;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * sys_user 基础Mapper。
 */
public interface SysUserMapper {
    int insert(SysUser sysUser);

    int updateById(SysUser sysUser);

    SysUser selectById(Long id);

    SysUser selectByUsernameOrPhone(@Param("loginName") String loginName);

    SysUser selectByUsername(@Param("username") String username);

    SysUser selectByPhone(@Param("phone") String phone);

    List<SysUser> selectPage(@Param("keyword") String keyword, @Param("offset") int offset, @Param("limit") int limit);

    long countPage(@Param("keyword") String keyword);

    List<SysUser> selectAll();

    int softDeleteById(Long id);
}
