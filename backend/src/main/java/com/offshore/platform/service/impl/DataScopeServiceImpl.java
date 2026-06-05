package com.offshore.platform.service.impl;

import com.offshore.platform.common.context.CurrentUser;
import com.offshore.platform.entity.ProjectInfo;
import com.offshore.platform.mapper.ProjectInfoMapper;
import com.offshore.platform.service.DataScopeService;
import org.springframework.stereotype.Service;

@Service
public class DataScopeServiceImpl implements DataScopeService {
    private final ProjectInfoMapper projectInfoMapper;

    public DataScopeServiceImpl(ProjectInfoMapper projectInfoMapper) {
        this.projectInfoMapper = projectInfoMapper;
    }

    @Override
    public String resolveDataScope(CurrentUser currentUser) {
        if (canAccessAll(currentUser)) {
            return "ALL";
        }
        if (hasRole(currentUser, "PROJECT_MANAGER") || hasRole(currentUser, "SITE_LEADER")) {
            return "PROJECT";
        }
        return currentUser.getDataScope() == null ? "SELF" : currentUser.getDataScope();
    }

    @Override
    public boolean canAccessAll(CurrentUser currentUser) {
        return currentUser != null
                && (hasRole(currentUser, "SYSTEM_ADMIN") || hasRole(currentUser, "SYS_ADMIN"));
    }

    @Override
    public boolean canAccessProject(CurrentUser currentUser, Long projectId) {
        if (currentUser == null || projectId == null) {
            return false;
        }
        if (canAccessAll(currentUser)) {
            return true;
        }
        if (!"PROJECT".equals(resolveDataScope(currentUser))) {
            return false;
        }
        if (projectId.equals(currentUser.getPrimaryProjectId())) {
            return true;
        }
        ProjectInfo project = projectInfoMapper.selectById(projectId);
        return project != null && currentUser.getUserId().equals(project.getProjectManagerId());
    }

    @Override
    public boolean canAccessOwnUser(CurrentUser currentUser, Long userId) {
        if (currentUser == null || userId == null) {
            return false;
        }
        return canAccessAll(currentUser) || userId.equals(currentUser.getUserId());
    }

    private boolean hasRole(CurrentUser currentUser, String roleCode) {
        return currentUser != null && currentUser.getRoleCodes().stream().anyMatch(role -> roleCode.equals(role));
    }
}
