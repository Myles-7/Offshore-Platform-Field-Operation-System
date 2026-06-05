package com.offshore.platform.service.impl;

import com.offshore.platform.entity.QualificationType;
import com.offshore.platform.mapper.QualificationTypeMapper;
import com.offshore.platform.service.QualificationTypeService;
import java.util.List;

import org.springframework.stereotype.Service;

/**
 * qualification_type 基础Service实现。
 */
@Service
public class QualificationTypeServiceImpl implements QualificationTypeService {
    private final QualificationTypeMapper qualificationTypeMapper;

    public QualificationTypeServiceImpl(QualificationTypeMapper qualificationTypeMapper) {
        this.qualificationTypeMapper = qualificationTypeMapper;
    }

    @Override
    public int create(QualificationType qualificationType) {
        return qualificationTypeMapper.insert(qualificationType);
    }

    @Override
    public int update(QualificationType qualificationType) {
        return qualificationTypeMapper.updateById(qualificationType);
    }

    @Override
    public QualificationType getById(Long id) {
        return qualificationTypeMapper.selectById(id);
    }

    @Override
    public List<QualificationType> listAll() {
        return qualificationTypeMapper.selectAll();
    }

    @Override
    public int removeById(Long id) {
        return qualificationTypeMapper.softDeleteById(id);
    }
}
