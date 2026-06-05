package com.offshore.platform.vo.qualification;

import java.time.LocalDate;

public class CertificateVO {
    public Long id;
    public Long employeeId;
    public Long qualificationTypeId;
    public String certificateNo;
    public String certificateName;
    public LocalDate validTo;
    public String validStatus;
    public String warningLevel;
    public String fileId;
}
