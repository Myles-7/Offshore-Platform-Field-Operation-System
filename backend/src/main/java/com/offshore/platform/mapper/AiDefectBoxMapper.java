package com.offshore.platform.mapper;

import com.offshore.platform.entity.AiDefectBox;
import java.util.List;

/**
 * ai_defect_box 基础Mapper。
 */
public interface AiDefectBoxMapper {
    int insert(AiDefectBox aiDefectBox);

    int updateById(AiDefectBox aiDefectBox);

    AiDefectBox selectById(Long id);

    List<AiDefectBox> selectAll();

    int softDeleteById(Long id);
}
