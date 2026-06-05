package com.offshore.platform.dto.acceptance;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SignatureRequest {
    public Long acceptanceId;
    @NotBlank(message = "签名文件ID不能为空")
    public String fileId;
    public String signatureRole;
    public String signerName;
    public String signerPhone;
    public LocalDateTime signedAt;
    public String signLocation;
    public BigDecimal latitude;
    public BigDecimal longitude;
    public String signatureHash;
    public String localId;
    public String deviceId;
    public String remark;
}
