package com.offshore.platform.dto.acceptance;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AcceptanceRequest {
    public Integer version;
    public String acceptanceStatus;
    public String acceptanceResult;
    public String acceptanceOpinion;
    public String rejectReason;
    public String problemDesc;
    public Integer rectificationRequired;
    public String recordSummary;
    public String attachmentSummary;
    public LocalDateTime acceptanceTime;
    public Long acceptorId;
    public List<Long> signatureIds = new ArrayList<>();
    public List<Long> recordIds = new ArrayList<>();
    public String localId;
    public String deviceId;
    public String remark;
}
