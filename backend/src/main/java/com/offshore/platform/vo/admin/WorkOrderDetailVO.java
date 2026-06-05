package com.offshore.platform.vo.admin;

import java.util.ArrayList;
import java.util.List;

public class WorkOrderDetailVO {
    public WorkOrderVO workOrder;
    public ProjectVO project;
    public List<AssignmentVO> assignments = new ArrayList<>();
    public List<StatusFlowVO> statusFlow = new ArrayList<>();
    public List<MaterialRequirementVO> requiredMaterials = new ArrayList<>();
    public List<SummaryVO> constructionRecordSummary = new ArrayList<>();
    public List<SummaryVO> attachmentSummary = new ArrayList<>();
    public List<SummaryVO> acceptanceRecords = new ArrayList<>();
    public List<SummaryVO> signatureRecords = new ArrayList<>();
    public List<SummaryVO> pdfRecords = new ArrayList<>();
    public List<SummaryVO> materialUsage = new ArrayList<>();
    public List<SummaryVO> aiResults = new ArrayList<>();
    public SyncSummaryVO syncSummary = new SyncSummaryVO();

    public static class AssignmentVO {
        public Long id;
        public Long workOrderId;
        public Long assignerId;
        public Long assigneeId;
        public String assignmentRole;
        public String assignmentStatus;
        public String assignedAt;
        public String remark;
    }

    public static class StatusFlowVO {
        public Long id;
        public String fromStatus;
        public String toStatus;
        public String operationType;
        public String operationDesc;
        public Long operatorId;
        public String operationTime;
    }

    public static class MaterialRequirementVO {
        public Long id;
        public String materialCode;
        public String materialName;
        public String materialSpec;
        public String unit;
        public String plannedQty;
        public String actualQty;
        public String prepareStatus;
    }

    public static class SummaryVO {
        public Long id;
        public String type;
        public String title;
        public String status;
        public String time;
    }

    public static class SyncSummaryVO {
        public String syncStatus;
        public Integer conflictCount = 0;
        public Boolean hasConflict = false;
    }
}
