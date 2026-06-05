package com.offshore.platform.vo.mobile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MobileWorkRecordVO {
    public Long id;
    public Long serverId;
    public String localId;
    public Long workOrderId;
    public Long projectId;
    public String recordNo;
    public String recordType;
    public LocalDateTime constructionTime;
    public Long constructionUserId;
    public String constructionUserName;
    public String constructionDesc;
    public String siteCondition;
    public Integer abnormalFlag;
    public String abnormalDesc;
    public String weather;
    public BigDecimal temperature;
    public BigDecimal humidity;
    public String locationName;
    public BigDecimal latitude;
    public BigDecimal longitude;
    public BigDecimal altitude;
    public Integer attachmentCount;
    public Integer aiResultCount;
    public String recordStatus;
    public LocalDateTime submittedAt;
    public Long confirmedBy;
    public LocalDateTime confirmedAt;
    public Integer version;
    public LocalDateTime updatedAt;
    public String syncStatus;
    public Integer conflictFlag;
    public List<MobileCheckItemVO> checkItems = new ArrayList<>();
    public List<MobileAttachmentVO> attachments = new ArrayList<>();
}
