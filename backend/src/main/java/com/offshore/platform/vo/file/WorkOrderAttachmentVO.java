package com.offshore.platform.vo.file;

import java.time.LocalDateTime;

public class WorkOrderAttachmentVO {
    public Long id;
    public Long serverId;
    public String localId;
    public Long workOrderId;
    public Long recordId;
    public String fileId;
    public String attachmentType;
    public String attachmentName;
    public String attachmentDesc;
    public String businessScene;
    public LocalDateTime captureTime;
    public String watermarkText;
    public String watermarkWorkOrderNo;
    public Integer version;
    public LocalDateTime updatedAt;
    public String syncStatus;
    public String previewUrl;
    public String downloadUrl;
}
