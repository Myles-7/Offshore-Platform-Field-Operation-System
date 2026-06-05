package com.offshore.platform.service;

import com.offshore.platform.dto.mobile.MobileCheckItemBatchRequest;
import com.offshore.platform.dto.mobile.MobileWorkRecordRequest;
import com.offshore.platform.vo.admin.WorkRecordTimelineVO;
import com.offshore.platform.vo.admin.WorkRecordVO;
import com.offshore.platform.vo.mobile.MobileCheckItemVO;
import com.offshore.platform.vo.mobile.MobileWorkRecordVO;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

public interface WorkRecordService {
    MobileWorkRecordVO createMobileRecord(Long workOrderId, MobileWorkRecordRequest request, HttpServletRequest servletRequest);

    MobileWorkRecordVO updateMobileRecord(Long workOrderId, Long recordId, MobileWorkRecordRequest request, HttpServletRequest servletRequest);

    List<MobileWorkRecordVO> listMobileRecords(Long workOrderId);

    List<MobileCheckItemVO> createCheckItems(Long recordId, MobileCheckItemBatchRequest request, HttpServletRequest servletRequest);

    List<WorkRecordVO> listAdminRecords(Long workOrderId);

    WorkRecordVO getAdminRecord(Long recordId);

    List<WorkRecordTimelineVO> getRecordTimeline(Long recordId);

    WorkRecordVO confirmRecord(Long recordId, HttpServletRequest servletRequest);
}
