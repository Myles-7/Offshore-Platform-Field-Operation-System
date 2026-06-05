package com.offshore.platform.service;

import com.offshore.platform.common.context.CurrentUser;
import com.offshore.platform.entity.FileStorage;

public interface FilePermissionService {
    void requireAccess(String fileId, CurrentUser currentUser);

    void requireAccess(FileStorage storage, CurrentUser currentUser);
}
