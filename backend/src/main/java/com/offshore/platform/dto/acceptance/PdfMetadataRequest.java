package com.offshore.platform.dto.acceptance;

import jakarta.validation.constraints.NotBlank;

public class PdfMetadataRequest {
    public Long acceptanceId;
    @NotBlank(message = "PDF文件ID不能为空")
    public String fileId;
    public String pdfContentSnapshot;
    public String localId;
    public String deviceId;
    public String remark;
}
