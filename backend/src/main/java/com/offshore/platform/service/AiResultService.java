package com.offshore.platform.service;

import com.offshore.platform.entity.AiResult;
import java.util.List;

/**
 * ai_result 基础Service。
 */
public interface AiResultService {
    int create(AiResult aiResult);

    int update(AiResult aiResult);

    AiResult getById(Long id);

    List<AiResult> listAll();

    int removeById(Long id);
}
