package com.offshore.platform.vo.workrecord;

import com.offshore.platform.vo.admin.AiSummaryVO;
import com.offshore.platform.vo.mobile.MobileAttachmentVO;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class WorkOrderRecordVO {
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
    public Integer attachmentCount;
    public Integer aiResultCount;
    public String recordStatus;
    public LocalDateTime submittedAt;
    public Integer version;
    public String syncStatus;
    public String deviceId;
    public Long operatorId;
    public Integer conflictFlag;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
    public List<WorkOrderRecordDetailVO> details = new ArrayList<>();
    public List<WorkOrderCheckItemVO> checkItems = new ArrayList<>();
    public List<MobileAttachmentVO> attachments = new ArrayList<>();
    public List<AiSummaryVO> aiResults = new ArrayList<>();
}
