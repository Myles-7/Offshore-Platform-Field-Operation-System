package com.offshore.platform.vo.admin;

import java.time.LocalDateTime;

public class AiSummaryVO {
    public Long id;
    public String aiResultNo;
    public String defectType;
    public Integer suspectedDefectFlag;
    public Integer defectCount;
    public String reviewStatus;
    public LocalDateTime inferTime;
}
