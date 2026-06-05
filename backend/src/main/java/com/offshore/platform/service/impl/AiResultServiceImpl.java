package com.offshore.platform.service.impl;

import com.offshore.platform.entity.AiResult;
import com.offshore.platform.mapper.AiResultMapper;
import com.offshore.platform.service.AiResultService;
import java.util.List;

import org.springframework.stereotype.Service;

/**
 * ai_result 基础Service实现。
 */
@Service
public class AiResultServiceImpl implements AiResultService {
    private final AiResultMapper aiResultMapper;

    public AiResultServiceImpl(AiResultMapper aiResultMapper) {
        this.aiResultMapper = aiResultMapper;
    }

    @Override
    public int create(AiResult aiResult) {
        return aiResultMapper.insert(aiResult);
    }

    @Override
    public int update(AiResult aiResult) {
        return aiResultMapper.updateById(aiResult);
    }

    @Override
    public AiResult getById(Long id) {
        return aiResultMapper.selectById(id);
    }

    @Override
    public List<AiResult> listAll() {
        return aiResultMapper.selectAll();
    }

    @Override
    public int removeById(Long id) {
        return aiResultMapper.softDeleteById(id);
    }
}
