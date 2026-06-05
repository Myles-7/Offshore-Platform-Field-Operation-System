package com.offshore.platform.service;

import com.offshore.platform.entity.AiModelInfo;
import java.util.List;

/**
 * ai_model_info 基础Service。
 */
public interface AiModelInfoService {
    int create(AiModelInfo aiModelInfo);

    int update(AiModelInfo aiModelInfo);

    AiModelInfo getById(Long id);

    List<AiModelInfo> listAll();

    int removeById(Long id);
}
