package com.offshore.platform.service.impl;

import com.offshore.platform.entity.AiReviewRecord;
import com.offshore.platform.mapper.AiReviewRecordMapper;
import com.offshore.platform.service.AiReviewRecordService;
import java.util.List;

import org.springframework.stereotype.Service;

/**
 * ai_review_record 基础Service实现。
 */
@Service
public class AiReviewRecordServiceImpl implements AiReviewRecordService {
    private final AiReviewRecordMapper aiReviewRecordMapper;

    public AiReviewRecordServiceImpl(AiReviewRecordMapper aiReviewRecordMapper) {
        this.aiReviewRecordMapper = aiReviewRecordMapper;
    }

    @Override
    public int create(AiReviewRecord aiReviewRecord) {
        return aiReviewRecordMapper.insert(aiReviewRecord);
    }

    @Override
    public int update(AiReviewRecord aiReviewRecord) {
        return aiReviewRecordMapper.updateById(aiReviewRecord);
    }

    @Override
    public AiReviewRecord getById(Long id) {
        return aiReviewRecordMapper.selectById(id);
    }

    @Override
    public List<AiReviewRecord> listAll() {
        return aiReviewRecordMapper.selectAll();
    }

    @Override
    public int removeById(Long id) {
        return aiReviewRecordMapper.softDeleteById(id);
    }
}
