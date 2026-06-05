package com.offshore.platform.mapper;

import com.offshore.platform.entity.MaterialInventory;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * material_inventory 基础Mapper。
 */
public interface MaterialInventoryMapper {
    int insert(MaterialInventory materialInventory);

    int updateById(MaterialInventory materialInventory);

    MaterialInventory selectById(Long id);

    List<MaterialInventory> selectByMaterialId(@Param("materialId") Long materialId);

    MaterialInventory selectOneForUpdate(@Param("materialId") Long materialId,
            @Param("warehouseCode") String warehouseCode, @Param("batchNo") String batchNo);

    List<MaterialInventory> selectAll();

    int softDeleteById(Long id);
}
