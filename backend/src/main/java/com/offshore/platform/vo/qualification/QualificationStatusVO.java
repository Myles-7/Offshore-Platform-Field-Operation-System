package com.offshore.platform.vo.qualification;

import java.util.ArrayList;
import java.util.List;

public class QualificationStatusVO {
    public Long employeeId;
    public String employeeName;
    public String overallStatus;
    public List<CertificateVO> certificates = new ArrayList<>();
}
