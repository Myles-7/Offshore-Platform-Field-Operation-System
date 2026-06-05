package com.offshore.platform.service.impl;

import com.offshore.platform.entity.AiModelInfo;
import com.offshore.platform.mapper.AiModelInfoMapper;
import com.offshore.platform.service.AiModelInfoService;
import java.util.List;

import org.springframework.stereotype.Service;

/**
 * ai_model_info 基础Service实现。
 */
@Service
public class AiModelInfoServiceImpl implements AiModelInfoService {
    private final AiModelInfoMapper aiModelInfoMapper;

    public AiModelInfoServiceImpl(AiModelInfoMapper aiModelInfoMapper) {
        this.aiModelInfoMapper = aiModelInfoMapper;
    }

    @Override
    public int create(AiModelInfo aiModelInfo) {
        return aiModelInfoMapper.insert(aiModelInfo);
    }

    @Override
    public int update(AiModelInfo aiModelInfo) {
        return aiModelInfoMapper.updateById(aiModelInfo);
    }

    @Override
    public AiModelInfo getById(Long id) {
        return aiModelInfoMapper.selectById(id);
    }

    @Override
    public List<AiModelInfo> listAll() {
        return aiModelInfoMapper.selectAll();
    }

    @Override
    public int removeById(Long id) {
        return aiModelInfoMapper.softDeleteById(id);
    }
}
