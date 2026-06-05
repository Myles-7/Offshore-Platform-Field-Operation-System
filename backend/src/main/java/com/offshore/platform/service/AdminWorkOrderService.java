package com.offshore.platform.service;

import com.offshore.platform.common.page.PageRequestDTO;
import com.offshore.platform.common.page.PageResult;
import com.offshore.platform.dto.admin.ProjectRequest;
import com.offshore.platform.dto.admin.WorkOrderAssignRequest;
import com.offshore.platform.dto.admin.WorkOrderFromTemplateRequest;
import com.offshore.platform.dto.admin.WorkOrderQueryRequest;
import com.offshore.platform.dto.admin.WorkOrderRequest;
import com.offshore.platform.dto.admin.WorkOrderStatusRequest;
import com.offshore.platform.dto.admin.WorkOrderTemplateRequest;
import com.offshore.platform.vo.admin.ProjectVO;
import com.offshore.platform.vo.admin.WorkOrderDetailVO;
import com.offshore.platform.vo.admin.WorkOrderTemplateVO;
import com.offshore.platform.vo.admin.WorkOrderVO;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

public interface AdminWorkOrderService {
    PageResult<ProjectVO> listProjects(PageRequestDTO pageRequest);

    ProjectVO getProject(Long id);

    ProjectVO createProject(ProjectRequest request, HttpServletRequest servletRequest);

    ProjectVO updateProject(Long id, ProjectRequest request, HttpServletRequest servletRequest);

    void deleteProject(Long id, HttpServletRequest servletRequest);

    PageResult<WorkOrderVO> listWorkOrders(WorkOrderQueryRequest query);

    WorkOrderDetailVO getWorkOrder(Long id);

    WorkOrderVO createWorkOrder(WorkOrderRequest request, HttpServletRequest servletRequest);

    WorkOrderVO updateWorkOrder(Long id, WorkOrderRequest request, HttpServletRequest servletRequest);

    void deleteWorkOrder(Long id, HttpServletRequest servletRequest);

    WorkOrderVO assignWorkOrder(Long id, WorkOrderAssignRequest request, HttpServletRequest servletRequest);

    WorkOrderVO changeWorkOrderStatus(Long id, WorkOrderStatusRequest request, HttpServletRequest servletRequest);

    List<WorkOrderDetailVO.StatusFlowVO> getStatusFlow(Long id);

    List<WorkOrderTemplateVO> listTemplates();

    WorkOrderTemplateVO createTemplate(WorkOrderTemplateRequest request, HttpServletRequest servletRequest);

    WorkOrderTemplateVO updateTemplate(Long id, WorkOrderTemplateRequest request, HttpServletRequest servletRequest);

    void deleteTemplate(Long id, HttpServletRequest servletRequest);

    WorkOrderVO createFromTemplate(Long templateId, WorkOrderFromTemplateRequest request, HttpServletRequest servletRequest);
}
