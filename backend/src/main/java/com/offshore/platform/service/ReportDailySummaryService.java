package com.offshore.platform.service;

import com.offshore.platform.entity.ReportDailySummary;
import java.util.List;

/**
 * report_daily_summary 基础Service。
 */
public interface ReportDailySummaryService {
    int create(ReportDailySummary reportDailySummary);

    int update(ReportDailySummary reportDailySummary);

    ReportDailySummary getById(Long id);

    List<ReportDailySummary> listAll();

    int removeById(Long id);
}
