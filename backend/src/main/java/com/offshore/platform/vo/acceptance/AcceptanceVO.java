package com.offshore.platform.vo.acceptance;

import java.time.LocalDateTime;

public class AcceptanceVO {
    public Long id;
    public Long serverId;
    public String localId;
    public String acceptanceNo;
    public Long workOrderId;
    public Long projectId;
    public String workOrderNo;
    public String projectName;
    public String acceptanceStatus;
    public String acceptanceResult;
    public String acceptanceOpinion;
    public String problemDesc;
    public Integer rectificationRequired;
    public Integer pdfGeneratedFlag;
    public Integer lockedFlag;
    public Integer signatureCount;
    public Integer version;
    public LocalDateTime updatedAt;
    public String syncStatus;
}
