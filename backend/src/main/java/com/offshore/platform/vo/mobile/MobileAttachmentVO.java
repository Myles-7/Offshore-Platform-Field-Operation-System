package com.offshore.platform.vo.mobile;

import java.time.LocalDateTime;

public class MobileAttachmentVO {
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
    public Long captureUserId;
    public String captureUserName;
    public String uploadStatus;
    public Integer version;
    public LocalDateTime updatedAt;
    public String syncStatus;
    public Integer conflictFlag;
}
