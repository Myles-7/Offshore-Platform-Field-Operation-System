package com.offshore.platform.service.impl;

import com.offshore.platform.entity.MaterialInoutRecord;
import com.offshore.platform.mapper.MaterialInoutRecordMapper;
import com.offshore.platform.service.MaterialInoutRecordService;
import java.util.List;

import org.springframework.stereotype.Service;

/**
 * material_inout_record 基础Service实现。
 */
@Service
public class MaterialInoutRecordServiceImpl implements MaterialInoutRecordService {
    private final MaterialInoutRecordMapper materialInoutRecordMapper;

    public MaterialInoutRecordServiceImpl(MaterialInoutRecordMapper materialInoutRecordMapper) {
        this.materialInoutRecordMapper = materialInoutRecordMapper;
    }

    @Override
    public int create(MaterialInoutRecord materialInoutRecord) {
        return materialInoutRecordMapper.insert(materialInoutRecord);
    }

    @Override
    public int update(MaterialInoutRecord materialInoutRecord) {
        return materialInoutRecordMapper.updateById(materialInoutRecord);
    }

    @Override
    public MaterialInoutRecord getById(Long id) {
        return materialInoutRecordMapper.selectById(id);
    }

    @Override
    public List<MaterialInoutRecord> listAll() {
        return materialInoutRecordMapper.selectAll();
    }

    @Override
    public int removeById(Long id) {
        return materialInoutRecordMapper.softDeleteById(id);
    }
}
