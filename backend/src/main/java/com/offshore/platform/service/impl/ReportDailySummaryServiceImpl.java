package com.offshore.platform.service.impl;

import com.offshore.platform.entity.ReportDailySummary;
import com.offshore.platform.mapper.ReportDailySummaryMapper;
import com.offshore.platform.service.ReportDailySummaryService;
import java.util.List;

import org.springframework.stereotype.Service;

/**
 * report_daily_summary 基础Service实现。
 */
@Service
public class ReportDailySummaryServiceImpl implements ReportDailySummaryService {
    private final ReportDailySummaryMapper reportDailySummaryMapper;

    public ReportDailySummaryServiceImpl(ReportDailySummaryMapper reportDailySummaryMapper) {
        this.reportDailySummaryMapper = reportDailySummaryMapper;
    }

    @Override
    public int create(ReportDailySummary reportDailySummary) {
        return reportDailySummaryMapper.insert(reportDailySummary);
    }

    @Override
    public int update(ReportDailySummary reportDailySummary) {
        return reportDailySummaryMapper.updateById(reportDailySummary);
    }

    @Override
    public ReportDailySummary getById(Long id) {
        return reportDailySummaryMapper.selectById(id);
    }

    @Override
    public List<ReportDailySummary> listAll() {
        return reportDailySummaryMapper.selectAll();
    }

    @Override
    public int removeById(Long id) {
        return reportDailySummaryMapper.softDeleteById(id);
    }
}
