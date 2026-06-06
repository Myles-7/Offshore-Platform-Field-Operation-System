package com.offshore.platform.vo.acceptance;

import java.time.LocalDateTime;

public class AcceptanceLockStatusVO {
    public Long workOrderId;
    public Long acceptanceId;
    public Long pdfId;
    public Integer lockedFlag;
    public Long lockedBy;
    public LocalDateTime lockedAt;
    public String lockReason;
}
