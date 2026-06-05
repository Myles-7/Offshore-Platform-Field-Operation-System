package com.offshore.platform.vo.acceptance;

import java.time.LocalDateTime;

public class SignatureVO {
    public Long id;
    public Long serverId;
    public String localId;
    public Long workOrderId;
    public Long acceptanceId;
    public String fileId;
    public String signatureRole;
    public Long signerUserId;
    public String signerName;
    public LocalDateTime signedAt;
    public String signatureStatus;
    public Integer version;
    public LocalDateTime updatedAt;
    public String syncStatus;
    public String previewUrl;
    public String downloadUrl;
}
