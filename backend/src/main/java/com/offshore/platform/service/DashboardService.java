package com.offshore.platform.service;

import com.offshore.platform.vo.DashboardVO;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

public interface DashboardService {
    DashboardVO overview();

    DashboardVO workOrderStatistics();

    List<Map<String, Object>> projectStatistics();

    List<Map<String, Object>> personStatistics();

    List<Map<String, Object>> materialStatistics();

    List<Map<String, Object>> outputValue();

    List<Map<String, Object>> reconciliation();

    byte[] exportReconciliation(HttpServletRequest request);
}
