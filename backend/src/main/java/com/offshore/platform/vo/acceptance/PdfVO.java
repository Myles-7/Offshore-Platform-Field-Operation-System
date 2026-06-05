package com.offshore.platform.vo.acceptance;

import java.time.LocalDateTime;

public class PdfVO {
    public Long id;
    public Long serverId;
    public String localId;
    public String pdfNo;
    public Long workOrderId;
    public Long acceptanceId;
    public String fileId;
    public String pdfStatus;
    public Integer lockedFlag;
    public LocalDateTime generatedAt;
    public String previewUrl;
    public String downloadUrl;
    public Integer version;
    public LocalDateTime updatedAt;
    public String syncStatus;
}
