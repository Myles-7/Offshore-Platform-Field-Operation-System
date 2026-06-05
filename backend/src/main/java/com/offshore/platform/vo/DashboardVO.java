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
    /** 资质临期数量（到期时间在未来30天内且未过期） */
    public Integer certificateExpiringCount;
    /** 库存预警数量（currentStock <= safetyStock） */
    public Integer inventoryWarningCount;
    /** 未处理同步冲突数量（resolveStatus = PENDING） */
    public Integer pendingConflictCount;
    /** AI待复核数量（reviewStatus = PENDING_REVIEW） */
    public Integer pendingAiReviewCount;
    public List<Map<String, Object>> items = new ArrayList<>();
}
