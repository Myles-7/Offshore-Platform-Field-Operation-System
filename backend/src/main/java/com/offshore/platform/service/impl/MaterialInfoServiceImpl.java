package com.offshore.platform.service.impl;

import com.offshore.platform.entity.MaterialInfo;
import com.offshore.platform.mapper.MaterialInfoMapper;
import com.offshore.platform.service.MaterialInfoService;
import java.util.List;

import org.springframework.stereotype.Service;

/**
 * material_info 基础Service实现。
 */
@Service
public class MaterialInfoServiceImpl implements MaterialInfoService {
    private final MaterialInfoMapper materialInfoMapper;

    public MaterialInfoServiceImpl(MaterialInfoMapper materialInfoMapper) {
        this.materialInfoMapper = materialInfoMapper;
    }

    @Override
    public int create(MaterialInfo materialInfo) {
        return materialInfoMapper.insert(materialInfo);
    }

    @Override
    public int update(MaterialInfo materialInfo) {
        return materialInfoMapper.updateById(materialInfo);
    }

    @Override
    public MaterialInfo getById(Long id) {
        return materialInfoMapper.selectById(id);
    }

    @Override
    public List<MaterialInfo> listAll() {
        return materialInfoMapper.selectAll();
    }

    @Override
    public int removeById(Long id) {
        return materialInfoMapper.softDeleteById(id);
    }
}
