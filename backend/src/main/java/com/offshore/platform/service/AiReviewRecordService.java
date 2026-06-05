package com.offshore.platform.service;

import com.offshore.platform.entity.AiReviewRecord;
import java.util.List;

/**
 * ai_review_record 基础Service。
 */
public interface AiReviewRecordService {
    int create(AiReviewRecord aiReviewRecord);

    int update(AiReviewRecord aiReviewRecord);

    AiReviewRecord getById(Long id);

    List<AiReviewRecord> listAll();

    int removeById(Long id);
}
