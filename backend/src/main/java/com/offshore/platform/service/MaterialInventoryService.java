package com.offshore.platform.service;

import com.offshore.platform.entity.MaterialInventory;
import java.util.List;

/**
 * material_inventory 基础Service。
 */
public interface MaterialInventoryService {
    int create(MaterialInventory materialInventory);

    int update(MaterialInventory materialInventory);

    MaterialInventory getById(Long id);

    List<MaterialInventory> listAll();

    int removeById(Long id);
}
