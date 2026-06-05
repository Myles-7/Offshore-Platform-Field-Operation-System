package com.offshore.platform.service.impl;

import com.offshore.platform.entity.WorkOrderPdf;
import com.offshore.platform.mapper.WorkOrderPdfMapper;
import com.offshore.platform.service.WorkOrderPdfService;
import java.util.List;

import org.springframework.stereotype.Service;

/**
 * work_order_pdf 基础Service实现。
 */
@Service
public class WorkOrderPdfServiceImpl implements WorkOrderPdfService {
    private final WorkOrderPdfMapper workOrderPdfMapper;

    public WorkOrderPdfServiceImpl(WorkOrderPdfMapper workOrderPdfMapper) {
        this.workOrderPdfMapper = workOrderPdfMapper;
    }

    @Override
    public int create(WorkOrderPdf workOrderPdf) {
        return workOrderPdfMapper.insert(workOrderPdf);
    }

    @Override
    public int update(WorkOrderPdf workOrderPdf) {
        return workOrderPdfMapper.updateById(workOrderPdf);
    }

    @Override
    public WorkOrderPdf getById(Long id) {
        return workOrderPdfMapper.selectById(id);
    }

    @Override
    public List<WorkOrderPdf> listAll() {
        return workOrderPdfMapper.selectAll();
    }

    @Override
    public int removeById(Long id) {
        return workOrderPdfMapper.softDeleteById(id);
    }
}
