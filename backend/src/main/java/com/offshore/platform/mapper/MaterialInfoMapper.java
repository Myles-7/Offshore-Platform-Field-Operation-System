package com.offshore.platform.mapper;

import com.offshore.platform.entity.MaterialInfo;
import java.util.List;

/**
 * material_info 基础Mapper。
 */
public interface MaterialInfoMapper {
    int insert(MaterialInfo materialInfo);

    int updateById(MaterialInfo materialInfo);

    MaterialInfo selectById(Long id);

    List<MaterialInfo> selectAll();

    int softDeleteById(Long id);
}
