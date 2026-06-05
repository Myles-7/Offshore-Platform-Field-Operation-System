package com.offshore.platform.mapper;

import com.offshore.platform.entity.AiModelInfo;
import java.util.List;

/**
 * ai_model_info 基础Mapper。
 */
public interface AiModelInfoMapper {
    int insert(AiModelInfo aiModelInfo);

    int updateById(AiModelInfo aiModelInfo);

    AiModelInfo selectById(Long id);

    List<AiModelInfo> selectAll();

    int softDeleteById(Long id);
}
