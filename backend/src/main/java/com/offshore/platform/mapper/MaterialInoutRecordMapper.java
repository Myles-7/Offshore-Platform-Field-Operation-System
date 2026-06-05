package com.offshore.platform.mapper;

import com.offshore.platform.entity.MaterialInoutRecord;
import java.util.List;

/**
 * material_inout_record 基础Mapper。
 */
public interface MaterialInoutRecordMapper {
    int insert(MaterialInoutRecord materialInoutRecord);

    int updateById(MaterialInoutRecord materialInoutRecord);

    MaterialInoutRecord selectById(Long id);

    List<MaterialInoutRecord> selectAll();

    int softDeleteById(Long id);
}
