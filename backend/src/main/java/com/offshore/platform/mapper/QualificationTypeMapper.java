package com.offshore.platform.mapper;

import com.offshore.platform.entity.QualificationType;
import java.util.List;

/**
 * qualification_type 基础Mapper。
 */
public interface QualificationTypeMapper {
    int insert(QualificationType qualificationType);

    int updateById(QualificationType qualificationType);

    QualificationType selectById(Long id);

    List<QualificationType> selectAll();

    int softDeleteById(Long id);
}
