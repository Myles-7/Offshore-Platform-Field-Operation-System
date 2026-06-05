package com.offshore.platform.mapper;

import com.offshore.platform.entity.AiReviewRecord;
import java.util.List;

/**
 * ai_review_record 基础Mapper。
 */
public interface AiReviewRecordMapper {
    int insert(AiReviewRecord aiReviewRecord);

    int updateById(AiReviewRecord aiReviewRecord);

    AiReviewRecord selectById(Long id);

    List<AiReviewRecord> selectAll();

    int softDeleteById(Long id);
}
