package com.offshore.platform.vo.file;

import java.time.LocalDateTime;

public class FileUploadVO {
    public Long id;
    public Long serverId;
    public String localId;
    public String fileId;
    public String fileType;
    public Long fileSize;
    public String mimeType;
    public String fileHash;
    public String previewUrl;
    public String downloadUrl;
    public String originalName;
    public Integer version;
    public LocalDateTime updatedAt;
    public String syncStatus;
}
