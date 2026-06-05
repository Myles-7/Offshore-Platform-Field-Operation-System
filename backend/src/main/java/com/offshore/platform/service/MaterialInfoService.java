package com.offshore.platform.service;

import com.offshore.platform.entity.MaterialInfo;
import java.util.List;

/**
 * material_info 基础Service。
 */
public interface MaterialInfoService {
    int create(MaterialInfo materialInfo);

    int update(MaterialInfo materialInfo);

    MaterialInfo getById(Long id);

    List<MaterialInfo> listAll();

    int removeById(Long id);
}
