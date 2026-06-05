package com.offshore.platform.dto.admin;

import com.offshore.platform.common.page.PageRequestDTO;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;

public class WorkOrderQueryRequest extends PageRequestDTO {
    private Long projectId;
    private String workOrderNo;
    private String workType;
    private String workLocation;
    private String status;
    private String priority;
    private Long maintainerId;
    private Long leaderId;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime plannedStartTimeStart;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime plannedStartTimeEnd;

    private String syncStatus;
    private Integer abnormalFlag;

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getWorkOrderNo() {
        return workOrderNo;
    }

    public void setWorkOrderNo(String workOrderNo) {
        this.workOrderNo = workOrderNo;
    }

    public String getWorkType() {
        return workType;
    }

    public void setWorkType(String workType) {
        this.workType = workType;
    }

    public String getWorkLocation() {
        return workLocation;
    }

    public void setWorkLocation(String workLocation) {
        this.workLocation = workLocation;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public Long getMaintainerId() {
        return maintainerId;
    }

    public void setMaintainerId(Long maintainerId) {
        this.maintainerId = maintainerId;
    }

    public Long getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(Long leaderId) {
        this.leaderId = leaderId;
    }

    public LocalDateTime getPlannedStartTimeStart() {
        return plannedStartTimeStart;
    }

    public void setPlannedStartTimeStart(LocalDateTime plannedStartTimeStart) {
        this.plannedStartTimeStart = plannedStartTimeStart;
    }

    public LocalDateTime getPlannedStartTimeEnd() {
        return plannedStartTimeEnd;
    }

    public void setPlannedStartTimeEnd(LocalDateTime plannedStartTimeEnd) {
        this.plannedStartTimeEnd = plannedStartTimeEnd;
    }

    public String getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(String syncStatus) {
        this.syncStatus = syncStatus;
    }

    public Integer getAbnormalFlag() {
        return abnormalFlag;
    }

    public void setAbnormalFlag(Integer abnormalFlag) {
        this.abnormalFlag = abnormalFlag;
    }
}
