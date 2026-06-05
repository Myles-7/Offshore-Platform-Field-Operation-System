package com.offshore.platform.vo.mobile;

import java.time.LocalDateTime;

public class MobileQualificationCheckVO {
    public Long id;
    public Long serverId;
    public String localId;
    public Long workOrderId;
    public Long employeeId;
    public Long certificateId;
    public Long qualificationTypeId;
    public String checkResult;
    public LocalDateTime checkTime;
    public Long checkerId;
    public Integer version;
    public LocalDateTime updatedAt;
    public String syncStatus;
}
