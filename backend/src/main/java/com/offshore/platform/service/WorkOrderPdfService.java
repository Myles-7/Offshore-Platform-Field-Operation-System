package com.offshore.platform.service;

import com.offshore.platform.entity.WorkOrderPdf;
import java.util.List;

/**
 * work_order_pdf 基础Service。
 */
public interface WorkOrderPdfService {
    int create(WorkOrderPdf workOrderPdf);

    int update(WorkOrderPdf workOrderPdf);

    WorkOrderPdf getById(Long id);

    List<WorkOrderPdf> listAll();

    int removeById(Long id);
}
