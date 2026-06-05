package com.offshore.platform.vo.mobile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MobileWorkOrderDetailVO extends MobileWorkOrderListVO {
    public Long projectId;
    public Long templateId;
    public String workType;
    public String workContent;
    public String requiredMaterialDesc;
    public Long leaderId;
    public Long maintainerId;
    public LocalDateTime actualStartTime;
    public LocalDateTime actualEndTime;
    public Integer acceptanceRequired;
    public String sourceType;
    public List<MobileAttachmentVO> attachments = new ArrayList<>();
    public List<MobileMaterialVO> materials = new ArrayList<>();
    public List<MobileStatusFlowVO> statusFlow = new ArrayList<>();
}
