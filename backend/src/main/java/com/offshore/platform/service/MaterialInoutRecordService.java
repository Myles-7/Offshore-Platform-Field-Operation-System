package com.offshore.platform.service;

import com.offshore.platform.entity.MaterialInoutRecord;
import java.util.List;

/**
 * material_inout_record 基础Service。
 */
public interface MaterialInoutRecordService {
    int create(MaterialInoutRecord materialInoutRecord);

    int update(MaterialInoutRecord materialInoutRecord);

    MaterialInoutRecord getById(Long id);

    List<MaterialInoutRecord> listAll();

    int removeById(Long id);
}
