package com.offshore.platform.service.impl;

import com.offshore.platform.common.context.CurrentUser;
import com.offshore.platform.service.DataScopeService;
import com.offshore.platform.service.ProjectScopeFilter;
import java.util.List;
import java.util.function.Function;
import org.springframework.stereotype.Service;

@Service
public class ProjectScopeFilterImpl implements ProjectScopeFilter {
    private final DataScopeService dataScopeService;

    public ProjectScopeFilterImpl(DataScopeService dataScopeService) {
        this.dataScopeService = dataScopeService;
    }

    @Override
    public <T> List<T> filter(CurrentUser currentUser, List<T> records, Function<T, Long> projectIdExtractor) {
        if (dataScopeService.canAccessAll(currentUser)) {
            return records;
        }
        return records.stream()
                .filter(record -> dataScopeService.canAccessProject(currentUser, projectIdExtractor.apply(record)))
                .toList();
    }
}
