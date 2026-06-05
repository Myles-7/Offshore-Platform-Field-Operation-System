package com.offshore.platform.service.impl;

import com.offshore.platform.common.context.CurrentUser;
import com.offshore.platform.common.context.CurrentUserContext;
import com.offshore.platform.common.enums.ErrorCode;
import com.offshore.platform.common.exception.BusinessException;
import com.offshore.platform.common.util.TraceIdUtils;
import com.offshore.platform.entity.OperationLog;
import com.offshore.platform.entity.ProjectInfo;
import com.offshore.platform.entity.ReportDailySummary;
import com.offshore.platform.entity.WorkOrder;
import com.offshore.platform.entity.WorkOrderMaterialUsage;
import com.offshore.platform.entity.WorkOrderRecord;
import com.offshore.platform.mapper.OperationLogMapper;
import com.offshore.platform.mapper.ProjectInfoMapper;
import com.offshore.platform.mapper.ReportDailySummaryMapper;
import com.offshore.platform.mapper.WorkOrderMapper;
import com.offshore.platform.mapper.WorkOrderMaterialUsageMapper;
import com.offshore.platform.mapper.WorkOrderRecordMapper;
import com.offshore.platform.service.DashboardService;
import com.offshore.platform.service.DataScopeService;
import com.offshore.platform.vo.DashboardVO;
import jakarta.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

@Service
public class DashboardServiceImpl implements DashboardService {
    private final WorkOrderMapper workOrderMapper;
    private final WorkOrderRecordMapper recordMapper;
    private final WorkOrderMaterialUsageMapper materialUsageMapper;
    private final ProjectInfoMapper projectMapper;
    private final ReportDailySummaryMapper reportMapper;
    private final OperationLogMapper operationLogMapper;
    private final DataScopeService dataScopeService;

    public DashboardServiceImpl(WorkOrderMapper workOrderMapper, WorkOrderRecordMapper recordMapper,
            WorkOrderMaterialUsageMapper materialUsageMapper, ProjectInfoMapper projectMapper,
            ReportDailySummaryMapper reportMapper, OperationLogMapper operationLogMapper,
            DataScopeService dataScopeService) {
        this.workOrderMapper = workOrderMapper;
        this.recordMapper = recordMapper;
        this.materialUsageMapper = materialUsageMapper;
        this.projectMapper = projectMapper;
        this.reportMapper = reportMapper;
        this.operationLogMapper = operationLogMapper;
        this.dataScopeService = dataScopeService;
    }

    @Override
    public DashboardVO overview() {
        CurrentUser user = CurrentUserContext.require();
        requireDashboardAccess(user);
        List<WorkOrder> orders = scopedOrders(user);
        DashboardVO vo = new DashboardVO();
        vo.inProgressWorkOrderCount = countStatus(orders, "IN_PROGRESS");
        vo.pendingAcceptanceWorkOrderCount = countStatus(orders, "PENDING_ACCEPTANCE");
        vo.abnormalWorkOrderCount = abnormalCount(orders);
        vo.todayAttendanceCount = todayAttendance(user);
        vo.weeklyCompletedOutputValue = weeklyOutput(user);
        vo.completionRate = completionRate(orders);
        return vo;
    }

    @Override
    public DashboardVO workOrderStatistics() {
        DashboardVO vo = overview();
        List<WorkOrder> orders = scopedOrders(CurrentUserContext.require());
        vo.items = orders.stream().collect(Collectors.groupingBy(WorkOrder::getStatus, Collectors.counting()))
                .entrySet().stream()
                .map(entry -> map("status", entry.getKey(), "count", entry.getValue()))
                .toList();
        return vo;
    }

    @Override
    public List<Map<String, Object>> projectStatistics() {
        CurrentUser user = CurrentUserContext.require();
        requireDashboardAccess(user);
        Map<Long, List<WorkOrder>> byProject = scopedOrders(user).stream().collect(Collectors.groupingBy(WorkOrder::getProjectId));
        return byProject.entrySet().stream().map(entry -> {
            ProjectInfo project = projectMapper.selectById(entry.getKey());
            List<WorkOrder> orders = entry.getValue();
            return map("projectId", entry.getKey(), "projectName", project == null ? null : project.getProjectName(),
                    "total", orders.size(), "completed", countStatus(orders, "COMPLETED"),
                    "inProgress", countStatus(orders, "IN_PROGRESS"), "completionRate", completionRate(orders));
        }).toList();
    }

    @Override
    public List<Map<String, Object>> personStatistics() {
        CurrentUser user = CurrentUserContext.require();
        requireDashboardAccess(user);
        return recordMapper.selectAll().stream()
                .filter(record -> canAccessProject(user, record.getProjectId()))
                .collect(Collectors.groupingBy(WorkOrderRecord::getConstructionUserId, Collectors.counting()))
                .entrySet().stream()
                .map(entry -> map("userId", entry.getKey(), "recordCount", entry.getValue()))
                .toList();
    }

    @Override
    public List<Map<String, Object>> materialStatistics() {
        CurrentUser user = CurrentUserContext.require();
        requireDashboardAccess(user);
        return materialUsageMapper.selectAll().stream()
                .filter(usage -> canAccessProject(user, usage.getProjectId()))
                .collect(Collectors.groupingBy(usage -> usage.getMaterialName() == null ? usage.getMaterialCode() : usage.getMaterialName(),
                        Collectors.reducing(BigDecimal.ZERO, this::safeUsedQty, BigDecimal::add)))
                .entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue(Comparator.reverseOrder()))
                .map(entry -> map("materialName", entry.getKey(), "usedQty", entry.getValue()))
                .toList();
    }

    @Override
    public List<Map<String, Object>> outputValue() {
        CurrentUser user = CurrentUserContext.require();
        requireDashboardAccess(user);
        return reportMapper.selectAll().stream()
                .filter(report -> canAccessProject(user, report.getProjectId()))
                .sorted(Comparator.comparing(ReportDailySummary::getSummaryDate))
                .map(report -> map("summaryDate", report.getSummaryDate(), "projectId", report.getProjectId(),
                        "projectName", report.getProjectName(), "outputValue", nullToZero(report.getOutputValue())))
                .toList();
    }

    @Override
    public List<Map<String, Object>> reconciliation() {
        CurrentUser user = CurrentUserContext.require();
        requireDashboardAccess(user);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (ProjectInfo project : projectMapper.selectAll()) {
            if (!canAccessProject(user, project.getId())) {
                continue;
            }
            List<WorkOrder> orders = scopedOrders(user).stream()
                    .filter(order -> project.getId().equals(order.getProjectId()))
                    .toList();
            BigDecimal materialAmount = materialUsageMapper.selectAll().stream()
                    .filter(usage -> project.getId().equals(usage.getProjectId()))
                    .map(usage -> nullToZero(usage.getCostAmount()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal output = reportMapper.selectAll().stream()
                    .filter(report -> project.getId().equals(report.getProjectId()))
                    .map(report -> nullToZero(report.getOutputValue()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            rows.add(map("projectId", project.getId(), "projectName", project.getProjectName(), "workOrderTotal", orders.size(),
                    "completed", countStatus(orders, "COMPLETED"), "materialAmount", materialAmount, "outputValue", output));
        }
        return rows;
    }

    @Override
    public byte[] exportReconciliation(HttpServletRequest request) {
        CurrentUser user = CurrentUserContext.require();
        requireDashboardAccess(user);
        List<Map<String, Object>> rows = reconciliation();
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("reconciliation");
            Row header = sheet.createRow(0);
            String[] columns = {"projectId", "projectName", "workOrderTotal", "completed", "materialAmount", "outputValue"};
            for (int i = 0; i < columns.length; i++) {
                header.createCell(i).setCellValue(columns[i]);
            }
            for (int i = 0; i < rows.size(); i++) {
                Row row = sheet.createRow(i + 1);
                Map<String, Object> data = rows.get(i);
                for (int c = 0; c < columns.length; c++) {
                    Object value = data.get(columns[c]);
                    row.createCell(c).setCellValue(value == null ? "" : String.valueOf(value));
                }
            }
            workbook.write(out);
            writeExportLog(user, request);
            return out.toByteArray();
        } catch (IOException ex) {
            throw new BusinessException(ErrorCode.DASHBOARD_ERROR, "Excel export failed");
        }
    }

    private List<WorkOrder> scopedOrders(CurrentUser user) {
        return workOrderMapper.selectAll().stream()
                .filter(order -> canAccessProject(user, order.getProjectId()))
                .toList();
    }

    private boolean canAccessProject(CurrentUser user, Long projectId) {
        return dataScopeService.canAccessAll(user) || dataScopeService.canAccessProject(user, projectId);
    }

    private void requireDashboardAccess(CurrentUser user) {
        if (dataScopeService.canAccessAll(user) || "DASHBOARD".equals(user.getDataScope())
                || user.getRoleCodes().contains("BUSINESS_USER") || user.getRoleCodes().contains("PROJECT_MANAGER")) {
            return;
        }
        throw new BusinessException(ErrorCode.FORBIDDEN, "No dashboard permission");
    }

    private int countStatus(List<WorkOrder> orders, String status) {
        return (int) orders.stream().filter(order -> status.equals(order.getStatus())).count();
    }

    private int abnormalCount(List<WorkOrder> orders) {
        Set<Long> orderIds = orders.stream().map(WorkOrder::getId).collect(Collectors.toSet());
        return (int) recordMapper.selectAll().stream()
                .filter(record -> orderIds.contains(record.getWorkOrderId()) && Integer.valueOf(1).equals(record.getAbnormalFlag()))
                .map(WorkOrderRecord::getWorkOrderId)
                .distinct()
                .count();
    }

    private int todayAttendance(CurrentUser user) {
        LocalDate today = LocalDate.now();
        return (int) recordMapper.selectAll().stream()
                .filter(record -> canAccessProject(user, record.getProjectId()))
                .filter(record -> record.getConstructionTime() != null && today.equals(record.getConstructionTime().toLocalDate()))
                .map(WorkOrderRecord::getConstructionUserId)
                .distinct()
                .count();
    }

    private BigDecimal weeklyOutput(CurrentUser user) {
        LocalDate today = LocalDate.now();
        LocalDate monday = today.with(DayOfWeek.MONDAY);
        return reportMapper.selectAll().stream()
                .filter(report -> canAccessProject(user, report.getProjectId()))
                .filter(report -> report.getSummaryDate() != null
                        && !report.getSummaryDate().isBefore(monday)
                        && !report.getSummaryDate().isAfter(today))
                .map(report -> nullToZero(report.getOutputValue()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal completionRate(List<WorkOrder> orders) {
        if (orders.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(countStatus(orders, "COMPLETED"))
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(orders.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal safeUsedQty(WorkOrderMaterialUsage usage) {
        return nullToZero(usage.getUsedQty());
    }

    private BigDecimal nullToZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private Map<String, Object> map(Object... keyValues) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            map.put(String.valueOf(keyValues[i]), keyValues[i + 1]);
        }
        return map;
    }

    private void writeExportLog(CurrentUser user, HttpServletRequest request) {
        OperationLog log = new OperationLog();
        log.setTraceId(TraceIdUtils.currentTraceId());
        log.setOperatorId(user.getUserId());
        log.setOperatorName(user.getRealName());
        log.setRoleCode(String.join(",", user.getRoleCodes()));
        log.setPlatform("PC");
        log.setModuleName("DASHBOARD");
        log.setOperationType("EXPORT_RECONCILIATION");
        log.setBusinessType("REPORT");
        log.setBusinessId("reconciliation");
        log.setBusinessNo("reconciliation");
        log.setRequestMethod(request.getMethod());
        log.setRequestPath(request.getRequestURI());
        log.setRequestIp(request.getRemoteAddr());
        log.setUserAgent(request.getHeader("User-Agent"));
        log.setResultStatus("SUCCESS");
        log.setOperationTime(LocalDateTime.now());
        log.setDeletedFlag(0);
        operationLogMapper.insert(log);
    }
}
