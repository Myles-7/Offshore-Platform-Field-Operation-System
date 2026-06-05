package com.offshore.platform.mapper;

import com.offshore.platform.entity.ReportDailySummary;
import java.util.List;

/**
 * report_daily_summary 基础Mapper。
 */
public interface ReportDailySummaryMapper {
    int insert(ReportDailySummary reportDailySummary);

    int updateById(ReportDailySummary reportDailySummary);

    ReportDailySummary selectById(Long id);

    List<ReportDailySummary> selectAll();

    int softDeleteById(Long id);
}
