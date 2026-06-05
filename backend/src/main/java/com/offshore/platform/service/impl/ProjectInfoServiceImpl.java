package com.offshore.platform.service.impl;

import com.offshore.platform.entity.ProjectInfo;
import com.offshore.platform.mapper.ProjectInfoMapper;
import com.offshore.platform.service.ProjectInfoService;
import java.util.List;

import org.springframework.stereotype.Service;

/**
 * project_info 基础Service实现。
 */
@Service
public class ProjectInfoServiceImpl implements ProjectInfoService {
    private final ProjectInfoMapper projectInfoMapper;

    public ProjectInfoServiceImpl(ProjectInfoMapper projectInfoMapper) {
        this.projectInfoMapper = projectInfoMapper;
    }

    @Override
    public int create(ProjectInfo projectInfo) {
        return projectInfoMapper.insert(projectInfo);
    }

    @Override
    public int update(ProjectInfo projectInfo) {
        return projectInfoMapper.updateById(projectInfo);
    }

    @Override
    public ProjectInfo getById(Long id) {
        return projectInfoMapper.selectById(id);
    }

    @Override
    public List<ProjectInfo> listAll() {
        return projectInfoMapper.selectAll();
    }

    @Override
    public int removeById(Long id) {
        return projectInfoMapper.softDeleteById(id);
    }
}
