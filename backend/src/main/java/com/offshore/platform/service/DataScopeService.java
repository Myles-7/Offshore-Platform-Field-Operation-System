package com.offshore.platform.service;

import com.offshore.platform.common.context.CurrentUser;

public interface DataScopeService {
    String resolveDataScope(CurrentUser currentUser);

    boolean canAccessAll(CurrentUser currentUser);

    boolean canAccessProject(CurrentUser currentUser, Long projectId);

    boolean canAccessOwnUser(CurrentUser currentUser, Long userId);
}
