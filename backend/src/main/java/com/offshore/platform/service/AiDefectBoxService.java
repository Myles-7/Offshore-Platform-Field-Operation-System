package com.offshore.platform.service;

import com.offshore.platform.entity.AiDefectBox;
import java.util.List;

/**
 * ai_defect_box 基础Service。
 */
public interface AiDefectBoxService {
    int create(AiDefectBox aiDefectBox);

    int update(AiDefectBox aiDefectBox);

    AiDefectBox getById(Long id);

    List<AiDefectBox> listAll();

    int removeById(Long id);
}
