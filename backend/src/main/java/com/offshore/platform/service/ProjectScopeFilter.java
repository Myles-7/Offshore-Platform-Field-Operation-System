package com.offshore.platform.service;

import com.offshore.platform.common.context.CurrentUser;
import java.util.List;
import java.util.function.Function;

public interface ProjectScopeFilter {
    <T> List<T> filter(CurrentUser currentUser, List<T> records, Function<T, Long> projectIdExtractor);
}
