package com.offshore.platform.dto.acceptance;

import jakarta.validation.constraints.NotBlank;

public class PdfMetadataRequest {
    public Long acceptanceId;
    @NotBlank(message = "PDF fileId is required")
    public String fileId;
    public String pdfNo;
    public String pdfStatus;
    public String pdfContentSnapshot;
    public String localId;
    public String deviceId;
    public String remark;
}
