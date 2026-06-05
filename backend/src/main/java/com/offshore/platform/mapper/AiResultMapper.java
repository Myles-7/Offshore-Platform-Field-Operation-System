package com.offshore.platform.mapper;

import com.offshore.platform.entity.AiResult;
import java.util.List;

/**
 * ai_result 基础Mapper。
 */
public interface AiResultMapper {
    int insert(AiResult aiResult);

    int updateById(AiResult aiResult);

    AiResult selectById(Long id);

    List<AiResult> selectAll();

    int softDeleteById(Long id);
}
