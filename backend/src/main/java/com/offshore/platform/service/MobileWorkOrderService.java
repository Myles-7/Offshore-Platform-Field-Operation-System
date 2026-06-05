package com.offshore.platform.service;

import com.offshore.platform.dto.mobile.MobileFeedbackRequest;
import com.offshore.platform.dto.mobile.MobileSubmitAcceptanceRequest;
import com.offshore.platform.vo.mobile.MobileMaterialVO;
import com.offshore.platform.vo.mobile.MobileQualificationCheckVO;
import com.offshore.platform.vo.mobile.MobileWorkOrderDetailVO;
import com.offshore.platform.vo.mobile.MobileWorkOrderListVO;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

public interface MobileWorkOrderService {
    List<MobileWorkOrderListVO> listMyWorkOrders();

    MobileWorkOrderDetailVO getMyWorkOrder(Long id);

    MobileWorkOrderDetailVO acceptWorkOrder(Long id, HttpServletRequest servletRequest);

    MobileWorkOrderDetailVO startWorkOrder(Long id, HttpServletRequest servletRequest);

    MobileWorkOrderDetailVO feedback(Long id, MobileFeedbackRequest request, HttpServletRequest servletRequest);

    MobileWorkOrderDetailVO submitAcceptance(Long id, MobileSubmitAcceptanceRequest request, HttpServletRequest servletRequest);

    List<MobileMaterialVO> listMaterials(Long id);

    List<MobileQualificationCheckVO> listQualificationChecks(Long id);
}
