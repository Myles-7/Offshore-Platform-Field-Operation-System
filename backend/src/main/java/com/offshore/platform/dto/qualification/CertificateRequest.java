package com.offshore.platform.dto.qualification;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class CertificateRequest {
    @NotNull(message = "资质类型不能为空")
    public Long qualificationTypeId;
    @NotBlank(message = "证书编号不能为空")
    public String certificateNo;
    public String certificateName;
    public String issueOrg;
    public LocalDate issueDate;
    public LocalDate validFrom;
    public LocalDate validTo;
    public String validStatus;
    public String warningLevel;
    public String fileId;
    public String remark;
}
