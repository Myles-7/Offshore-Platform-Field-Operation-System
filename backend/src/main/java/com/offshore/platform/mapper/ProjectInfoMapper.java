package com.offshore.platform.mapper;

import com.offshore.platform.entity.ProjectInfo;
import java.util.List;

/**
 * project_info 基础Mapper。
 */
public interface ProjectInfoMapper {
    int insert(ProjectInfo projectInfo);

    int updateById(ProjectInfo projectInfo);

    ProjectInfo selectById(Long id);

    List<ProjectInfo> selectAll();

    int softDeleteById(Long id);
}
