package com.offshore.platform.service;

import com.offshore.platform.dto.mobile.MobileCheckItemBatchRequest;
import com.offshore.platform.dto.mobile.MobileWorkRecordRequest;
import com.offshore.platform.dto.workrecord.WorkOrderCheckItemBatchRequest;
import com.offshore.platform.dto.workrecord.WorkOrderCheckItemRequest;
import com.offshore.platform.dto.workrecord.WorkOrderRecordCreateRequest;
import com.offshore.platform.dto.workrecord.WorkOrderRecordDetailRequest;
import com.offshore.platform.dto.workrecord.WorkOrderRecordQueryRequest;
import com.offshore.platform.dto.workrecord.WorkOrderRecordRejectRequest;
import com.offshore.platform.dto.workrecord.WorkOrderRecordUpdateRequest;
import com.offshore.platform.vo.admin.WorkRecordTimelineVO;
import com.offshore.platform.vo.admin.WorkRecordVO;
import com.offshore.platform.vo.mobile.MobileCheckItemVO;
import com.offshore.platform.vo.mobile.MobileWorkRecordVO;
import com.offshore.platform.vo.workrecord.WorkOrderCheckItemVO;
import com.offshore.platform.vo.workrecord.WorkOrderRecordDetailVO;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

public interface WorkRecordService {
    MobileWorkRecordVO createMobileRecord(Long workOrderId, MobileWorkRecordRequest request, HttpServletRequest servletRequest);

    MobileWorkRecordVO updateMobileRecord(Long workOrderId, Long recordId, MobileWorkRecordRequest request, HttpServletRequest servletRequest);

    List<MobileWorkRecordVO> listMobileRecords(Long workOrderId);

    com.offshore.platform.vo.workrecord.WorkOrderRecordVO getMobileRecord(Long workOrderId, Long recordId);

    com.offshore.platform.vo.workrecord.WorkOrderRecordVO createMobileRecord(Long workOrderId, WorkOrderRecordCreateRequest request, HttpServletRequest servletRequest);

    com.offshore.platform.vo.workrecord.WorkOrderRecordVO updateMobileRecord(Long workOrderId, Long recordId, WorkOrderRecordUpdateRequest request, HttpServletRequest servletRequest);

    void deleteMobileRecord(Long workOrderId, Long recordId, HttpServletRequest servletRequest);

    WorkOrderRecordDetailVO createDetail(Long recordId, WorkOrderRecordDetailRequest request, HttpServletRequest servletRequest);

    WorkOrderRecordDetailVO updateDetail(Long recordId, Long detailId, WorkOrderRecordDetailRequest request, HttpServletRequest servletRequest);

    void deleteDetail(Long recordId, Long detailId, HttpServletRequest servletRequest);

    List<MobileCheckItemVO> createCheckItems(Long recordId, MobileCheckItemBatchRequest request, HttpServletRequest servletRequest);

    List<WorkOrderCheckItemVO> saveCheckItems(Long recordId, WorkOrderCheckItemBatchRequest request, HttpServletRequest servletRequest);

    WorkOrderCheckItemVO updateCheckItem(Long recordId, Long itemId, WorkOrderCheckItemRequest request, HttpServletRequest servletRequest);

    void deleteCheckItem(Long recordId, Long itemId, HttpServletRequest servletRequest);

    List<WorkRecordVO> listAdminRecords(Long workOrderId);

    List<com.offshore.platform.vo.workrecord.WorkOrderRecordVO> listAdminRecords(Long workOrderId, WorkOrderRecordQueryRequest request);

    WorkRecordVO getAdminRecord(Long recordId);

    com.offshore.platform.vo.workrecord.WorkOrderRecordVO getAdminRecordDetail(Long recordId);

    List<WorkRecordTimelineVO> getRecordTimeline(Long recordId);

    WorkRecordVO confirmRecord(Long recordId, HttpServletRequest servletRequest);

    com.offshore.platform.vo.workrecord.WorkOrderRecordVO rejectRecord(Long recordId, WorkOrderRecordRejectRequest request, HttpServletRequest servletRequest);
}
