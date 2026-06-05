package com.offshore.platform.service;

import com.offshore.platform.entity.ProjectInfo;
import java.util.List;

/**
 * project_info 基础Service。
 */
public interface ProjectInfoService {
    int create(ProjectInfo projectInfo);

    int update(ProjectInfo projectInfo);

    ProjectInfo getById(Long id);

    List<ProjectInfo> listAll();

    int removeById(Long id);
}
