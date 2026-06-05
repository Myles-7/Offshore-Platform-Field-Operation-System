package com.offshore.platform.vo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DashboardVO {
    public Integer inProgressWorkOrderCount;
    public Integer todayAttendanceCount;
    public BigDecimal weeklyCompletedOutputValue;
    public Integer pendingAcceptanceWorkOrderCount;
    public Integer abnormalWorkOrderCount;
    public BigDecimal completionRate;
    public List<Map<String, Object>> items = new ArrayList<>();
}
