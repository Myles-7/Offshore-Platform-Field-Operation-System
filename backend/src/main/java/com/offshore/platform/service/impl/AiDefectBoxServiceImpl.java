package com.offshore.platform.service.impl;

import com.offshore.platform.entity.AiDefectBox;
import com.offshore.platform.mapper.AiDefectBoxMapper;
import com.offshore.platform.service.AiDefectBoxService;
import java.util.List;

import org.springframework.stereotype.Service;

/**
 * ai_defect_box 基础Service实现。
 */
@Service
public class AiDefectBoxServiceImpl implements AiDefectBoxService {
    private final AiDefectBoxMapper aiDefectBoxMapper;

    public AiDefectBoxServiceImpl(AiDefectBoxMapper aiDefectBoxMapper) {
        this.aiDefectBoxMapper = aiDefectBoxMapper;
    }

    @Override
    public int create(AiDefectBox aiDefectBox) {
        return aiDefectBoxMapper.insert(aiDefectBox);
    }

    @Override
    public int update(AiDefectBox aiDefectBox) {
        return aiDefectBoxMapper.updateById(aiDefectBox);
    }

    @Override
    public AiDefectBox getById(Long id) {
        return aiDefectBoxMapper.selectById(id);
    }

    @Override
    public List<AiDefectBox> listAll() {
        return aiDefectBoxMapper.selectAll();
    }

    @Override
    public int removeById(Long id) {
        return aiDefectBoxMapper.softDeleteById(id);
    }
}
