package com.offshore.platform.service.impl;

import com.offshore.platform.entity.MaterialInventory;
import com.offshore.platform.mapper.MaterialInventoryMapper;
import com.offshore.platform.service.MaterialInventoryService;
import java.util.List;

import org.springframework.stereotype.Service;

/**
 * material_inventory 基础Service实现。
 */
@Service
public class MaterialInventoryServiceImpl implements MaterialInventoryService {
    private final MaterialInventoryMapper materialInventoryMapper;

    public MaterialInventoryServiceImpl(MaterialInventoryMapper materialInventoryMapper) {
        this.materialInventoryMapper = materialInventoryMapper;
    }

    @Override
    public int create(MaterialInventory materialInventory) {
        return materialInventoryMapper.insert(materialInventory);
    }

    @Override
    public int update(MaterialInventory materialInventory) {
        return materialInventoryMapper.updateById(materialInventory);
    }

    @Override
    public MaterialInventory getById(Long id) {
        return materialInventoryMapper.selectById(id);
    }

    @Override
    public List<MaterialInventory> listAll() {
        return materialInventoryMapper.selectAll();
    }

    @Override
    public int removeById(Long id) {
        return materialInventoryMapper.softDeleteById(id);
    }
}
