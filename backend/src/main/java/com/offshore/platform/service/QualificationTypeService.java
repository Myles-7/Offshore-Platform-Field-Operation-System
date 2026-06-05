package com.offshore.platform.service;

import com.offshore.platform.entity.QualificationType;
import java.util.List;

/**
 * qualification_type 基础Service。
 */
public interface QualificationTypeService {
    int create(QualificationType qualificationType);

    int update(QualificationType qualificationType);

    QualificationType getById(Long id);

    List<QualificationType> listAll();

    int removeById(Long id);
}
