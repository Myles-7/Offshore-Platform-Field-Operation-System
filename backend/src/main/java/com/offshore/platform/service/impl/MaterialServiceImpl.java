package com.offshore.platform.service.impl;

import com.offshore.platform.common.context.CurrentUser;
import com.offshore.platform.common.context.CurrentUserContext;
import com.offshore.platform.common.enums.ErrorCode;
import com.offshore.platform.common.exception.BusinessException;
import com.offshore.platform.common.util.TraceIdUtils;
import com.offshore.platform.dto.material.MaterialInoutRequest;
import com.offshore.platform.dto.material.MaterialQrcodeRequest;
import com.offshore.platform.dto.material.MaterialRequest;
import com.offshore.platform.dto.material.MaterialUsageRequest;
import com.offshore.platform.entity.MaterialInfo;
import com.offshore.platform.entity.MaterialInventory;
import com.offshore.platform.entity.MaterialInoutRecord;
import com.offshore.platform.entity.MaterialQrcode;
import com.offshore.platform.entity.OperationLog;
import com.offshore.platform.entity.WorkOrder;
import com.offshore.platform.entity.WorkOrderAssignment;
import com.offshore.platform.entity.WorkOrderMaterial;
import com.offshore.platform.entity.WorkOrderMaterialUsage;
import com.offshore.platform.mapper.MaterialInfoMapper;
import com.offshore.platform.mapper.MaterialInventoryMapper;
import com.offshore.platform.mapper.MaterialInoutRecordMapper;
import com.offshore.platform.mapper.MaterialQrcodeMapper;
import com.offshore.platform.mapper.OperationLogMapper;
import com.offshore.platform.mapper.WorkOrderAssignmentMapper;
import com.offshore.platform.mapper.WorkOrderMapper;
import com.offshore.platform.mapper.WorkOrderMaterialMapper;
import com.offshore.platform.mapper.WorkOrderMaterialUsageMapper;
import com.offshore.platform.service.DataScopeService;
import com.offshore.platform.service.MaterialService;
import com.offshore.platform.vo.material.InventoryVO;
import com.offshore.platform.vo.material.MaterialInoutVO;
import com.offshore.platform.vo.material.MaterialQrcodeVO;
import com.offshore.platform.vo.material.MaterialUsageVO;
import com.offshore.platform.vo.material.MaterialVO;
import com.offshore.platform.vo.mobile.MobileMaterialVO;
import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class MaterialServiceImpl implements MaterialService {
    private final MaterialInfoMapper materialInfoMapper;
    private final MaterialInventoryMapper inventoryMapper;
    private final MaterialInoutRecordMapper inoutMapper;
    private final MaterialQrcodeMapper qrcodeMapper;
    private final WorkOrderMaterialMapper workOrderMaterialMapper;
    private final WorkOrderMaterialUsageMapper usageMapper;
    private final WorkOrderMapper workOrderMapper;
    private final WorkOrderAssignmentMapper assignmentMapper;
    private final OperationLogMapper operationLogMapper;
    private final DataScopeService dataScopeService;

    public MaterialServiceImpl(MaterialInfoMapper materialInfoMapper, MaterialInventoryMapper inventoryMapper,
            MaterialInoutRecordMapper inoutMapper, MaterialQrcodeMapper qrcodeMapper,
            WorkOrderMaterialMapper workOrderMaterialMapper, WorkOrderMaterialUsageMapper usageMapper,
            WorkOrderMapper workOrderMapper, WorkOrderAssignmentMapper assignmentMapper,
            OperationLogMapper operationLogMapper, DataScopeService dataScopeService) {
        this.materialInfoMapper = materialInfoMapper;
        this.inventoryMapper = inventoryMapper;
        this.inoutMapper = inoutMapper;
        this.qrcodeMapper = qrcodeMapper;
        this.workOrderMaterialMapper = workOrderMaterialMapper;
        this.usageMapper = usageMapper;
        this.workOrderMapper = workOrderMapper;
        this.assignmentMapper = assignmentMapper;
        this.operationLogMapper = operationLogMapper;
        this.dataScopeService = dataScopeService;
    }

    @Override public List<MaterialVO> listMaterials() { requireAdmin(CurrentUserContext.require()); return materialInfoMapper.selectAll().stream().map(this::toMaterialVO).toList(); }
    @Override public MaterialVO getMaterial(Long id) { requireAdmin(CurrentUserContext.require()); return toMaterialVO(requireMaterial(id)); }

    @Override @Transactional
    public MaterialVO createMaterial(MaterialRequest request, HttpServletRequest servletRequest) {
        CurrentUser user = CurrentUserContext.require(); requireAdmin(user);
        MaterialInfo material = new MaterialInfo(); fillMaterial(material, request, user, true); materialInfoMapper.insert(material);
        writeLog(user, servletRequest, "CREATE_MATERIAL", "MATERIAL", material.getId(), material.getMaterialCode(), null);
        return toMaterialVO(material);
    }

    @Override @Transactional
    public MaterialVO updateMaterial(Long id, MaterialRequest request, HttpServletRequest servletRequest) {
        CurrentUser user = CurrentUserContext.require(); requireAdmin(user);
        MaterialInfo material = requireMaterial(id); fillMaterial(material, request, user, false); materialInfoMapper.updateById(material);
        writeLog(user, servletRequest, "UPDATE_MATERIAL", "MATERIAL", material.getId(), material.getMaterialCode(), null);
        return toMaterialVO(material);
    }

    @Override @Transactional
    public void deleteMaterial(Long id, HttpServletRequest servletRequest) {
        CurrentUser user = CurrentUserContext.require(); requireAdmin(user); MaterialInfo material = requireMaterial(id);
        materialInfoMapper.softDeleteById(id);
        writeLog(user, servletRequest, "DELETE_MATERIAL", "MATERIAL", id, material.getMaterialCode(), null);
    }

    @Override public List<InventoryVO> listInventory(Long materialId) { requireAdmin(CurrentUserContext.require()); requireMaterial(materialId); return inventoryMapper.selectByMaterialId(materialId).stream().map(this::toInventoryVO).toList(); }
    @Override @Transactional public MaterialInoutVO inbound(MaterialInoutRequest request, HttpServletRequest servletRequest) { return adjustInventory("IN", request, servletRequest); }
    @Override @Transactional public MaterialInoutVO outbound(MaterialInoutRequest request, HttpServletRequest servletRequest) { return adjustInventory("OUT", request, servletRequest); }
    @Override @Transactional public MaterialInoutVO stocktaking(MaterialInoutRequest request, HttpServletRequest servletRequest) { return adjustInventory("CHECK", request, servletRequest); }

    @Override @Transactional
    public MaterialQrcodeVO createQrcode(Long materialId, MaterialQrcodeRequest request, HttpServletRequest servletRequest) {
        CurrentUser user = CurrentUserContext.require(); requireAdmin(user); MaterialInfo material = requireMaterial(materialId);
        MaterialQrcode qrcode = new MaterialQrcode();
        qrcode.setMaterialId(materialId); qrcode.setMaterialCode(material.getMaterialCode());
        qrcode.setQrcodeValue("MAT-" + material.getMaterialCode() + "-" + nowNo());
        qrcode.setQrcodeFileId(request.qrcodeFileId); qrcode.setBatchNo(request.batchNo); qrcode.setSerialNo(request.serialNo);
        qrcode.setGenerateUserId(user.getUserId()); qrcode.setGenerateTime(LocalDateTime.now());
        qrcode.setBindStatus("BOUND"); qrcode.setQrcodeStatus("ACTIVE"); qrcode.setCreatedAt(LocalDateTime.now()); qrcode.setUpdatedAt(LocalDateTime.now()); qrcode.setDeletedFlag(0); qrcode.setCreatedBy(user.getUserId()); qrcode.setUpdatedBy(user.getUserId()); qrcode.setRemark(request.remark);
        qrcodeMapper.insert(qrcode);
        writeLog(user, servletRequest, "CREATE_MATERIAL_QRCODE", "MATERIAL_QRCODE", qrcode.getId(), qrcode.getQrcodeValue(), null);
        return toQrcodeVO(qrcode);
    }

    @Override
    public MaterialQrcodeVO getQrcode(String code) {
        CurrentUser user = CurrentUserContext.require();
        MaterialQrcode qrcode = qrcodeMapper.selectByQrcodeValue(code);
        if (qrcode == null) throw new BusinessException(ErrorCode.NOT_FOUND, "二维码不存在");
        qrcode.setLastScanTime(LocalDateTime.now()); qrcode.setLastScanUserId(user.getUserId()); qrcode.setUpdatedAt(LocalDateTime.now()); qrcodeMapper.updateById(qrcode);
        return toQrcodeVO(qrcode);
    }

    @Override public List<MaterialUsageVO> listWorkOrderUsage(Long workOrderId) { WorkOrder order = requireAdminWorkOrder(workOrderId, CurrentUserContext.require()); return usageMapper.selectByWorkOrderId(order.getId()).stream().map(this::toUsageVO).toList(); }
    @Override public List<MobileMaterialVO> listMobileRequirements(Long workOrderId) { WorkOrder order = requireMobileWorkOrder(workOrderId, CurrentUserContext.require()); return workOrderMaterialMapper.selectByWorkOrderId(order.getId()).stream().map(this::toMobileMaterialVO).toList(); }

    @Override @Transactional
    public MaterialUsageVO createMobileUsage(Long workOrderId, MaterialUsageRequest request, HttpServletRequest servletRequest) {
        CurrentUser user = CurrentUserContext.require(); WorkOrder order = requireMobileWorkOrder(workOrderId, user); MaterialInfo material = requireMaterial(request.materialId);
        MaterialInoutRequest out = new MaterialInoutRequest(); out.materialId = request.materialId; out.quantity = request.usedQty; out.workOrderId = workOrderId; out.qrcodeId = request.qrcodeId; out.qrcodeValue = request.qrcodeValue; out.warehouseCode = "DEFAULT"; out.warehouseName = "默认仓库"; out.businessReason = "WORK_ORDER_USAGE";
        MaterialInoutRecord inout = adjustInventoryEntity("OUT", out, user, order);
        WorkOrderMaterialUsage usage = new WorkOrderMaterialUsage();
        usage.setUsageNo("USE-" + nowNo()); usage.setWorkOrderId(workOrderId); usage.setWorkOrderNo(order.getWorkOrderNo()); usage.setProjectId(order.getProjectId()); usage.setMaterialId(material.getId()); usage.setMaterialCode(material.getMaterialCode()); usage.setMaterialName(material.getMaterialName()); usage.setMaterialSpec(material.getMaterialSpec()); usage.setUnit(material.getUnit()); usage.setQrcodeId(request.qrcodeId); usage.setQrcodeValue(request.qrcodeValue); usage.setPlannedQty(request.plannedQty); usage.setUsedQty(request.usedQty); usage.setWasteQty(nvl(request.wasteQty)); usage.setReturnQty(nvl(request.returnQty)); usage.setUsageTime(request.usageTime == null ? LocalDateTime.now() : request.usageTime); usage.setUsageUserId(user.getUserId()); usage.setUsageUserName(user.getRealName()); usage.setUsageLocation(request.usageLocation); usage.setUsageDesc(request.usageDesc); usage.setCostPrice(request.costPrice); usage.setCostAmount(request.costPrice == null ? null : request.costPrice.multiply(request.usedQty)); usage.setSourceType("MOBILE"); usage.setInoutRecordId(inout.getId()); usage.setLocalId(request.localId); usage.setServerId(null); usage.setVersion(1); usage.setSyncStatus("SYNCED"); usage.setDeviceId(request.deviceId); usage.setOperatorId(user.getUserId()); usage.setConflictFlag(0); usage.setCreatedAt(LocalDateTime.now()); usage.setUpdatedAt(LocalDateTime.now()); usage.setDeletedFlag(0); usage.setCreatedBy(user.getUserId()); usage.setUpdatedBy(user.getUserId()); usage.setRemark(request.remark);
        usageMapper.insert(usage); usage.setServerId(usage.getId()); usageMapper.updateById(usage);
        writeLog(user, servletRequest, "CREATE_WORK_ORDER_MATERIAL_USAGE", "WORK_ORDER_MATERIAL_USAGE", usage.getId(), order.getWorkOrderNo(), order.getProjectId());
        return toUsageVO(usage);
    }

    private MaterialInoutVO adjustInventory(String type, MaterialInoutRequest request, HttpServletRequest servletRequest) {
        CurrentUser user = CurrentUserContext.require(); requireAdmin(user); WorkOrder order = request.workOrderId == null ? null : requireAdminWorkOrder(request.workOrderId, user);
        MaterialInoutRecord record = adjustInventoryEntity(type, request, user, order);
        writeLog(user, servletRequest, type.equals("IN") ? "MATERIAL_INBOUND" : type.equals("OUT") ? "MATERIAL_OUTBOUND" : "MATERIAL_STOCKTAKING", "MATERIAL_INOUT", record.getId(), record.getRecordNo(), order == null ? null : order.getProjectId());
        return toInoutVO(record);
    }

    private MaterialInoutRecord adjustInventoryEntity(String type, MaterialInoutRequest request, CurrentUser user, WorkOrder order) {
        MaterialInfo material = requireMaterial(request.materialId);
        String warehouse = StringUtils.hasText(request.warehouseCode) ? request.warehouseCode : "DEFAULT";
        MaterialInventory inventory = inventoryMapper.selectOneForUpdate(request.materialId, warehouse, request.batchNo);
        BigDecimal before = inventory == null ? BigDecimal.ZERO : nvl(inventory.getCurrentQty());
        BigDecimal qty = request.quantity;
        BigDecimal after = switch (type) { case "IN" -> before.add(qty); case "OUT" -> before.subtract(qty); case "CHECK" -> qty; default -> before; };
        if (after.compareTo(BigDecimal.ZERO) < 0) throw new BusinessException(ErrorCode.MATERIAL_ERROR, "库存不足，当前可用库存为 " + before);
        if (inventory == null) {
            inventory = new MaterialInventory(); inventory.setMaterialId(material.getId()); inventory.setMaterialCode(material.getMaterialCode()); inventory.setWarehouseCode(warehouse); inventory.setWarehouseName(defaultText(request.warehouseName, "默认仓库")); inventory.setLocationCode(request.locationCode); inventory.setBatchNo(request.batchNo); inventory.setQrcodeId(request.qrcodeId); inventory.setLockedQty(BigDecimal.ZERO); inventory.setCreatedAt(LocalDateTime.now()); inventory.setDeletedFlag(0); inventory.setCreatedBy(user.getUserId());
        }
        inventory.setCurrentQty(after); inventory.setAvailableQty(after.subtract(nvl(inventory.getLockedQty()))); inventory.setInventoryStatus("NORMAL"); inventory.setUpdatedAt(LocalDateTime.now()); inventory.setUpdatedBy(user.getUserId());
        if ("IN".equals(type)) inventory.setLastInTime(LocalDateTime.now()); if ("OUT".equals(type)) inventory.setLastOutTime(LocalDateTime.now()); if ("CHECK".equals(type)) inventory.setLastCheckTime(LocalDateTime.now());
        if (inventory.getId() == null) inventoryMapper.insert(inventory); else inventoryMapper.updateById(inventory);
        MaterialInoutRecord record = new MaterialInoutRecord(); record.setRecordNo("IO-" + nowNo()); record.setMaterialId(material.getId()); record.setMaterialCode(material.getMaterialCode()); record.setMaterialName(material.getMaterialName()); record.setQrcodeId(request.qrcodeId); record.setQrcodeValue(request.qrcodeValue); record.setProjectId(order == null ? null : order.getProjectId()); record.setWorkOrderId(order == null ? null : order.getId()); record.setWorkOrderNo(order == null ? null : order.getWorkOrderNo()); record.setInoutType(type); record.setQuantity(qty); record.setBeforeQty(before); record.setAfterQty(after); record.setWarehouseCode(warehouse); record.setWarehouseName(defaultText(request.warehouseName, "默认仓库")); record.setLocationCode(request.locationCode); record.setBatchNo(request.batchNo); record.setSourceType(order == null ? "ADMIN" : "WORK_ORDER"); record.setBusinessReason(request.businessReason); record.setOperatorId(user.getUserId()); record.setOperatorName(user.getRealName()); record.setOperateTime(LocalDateTime.now()); record.setApprovalStatus("APPROVED"); record.setCreatedAt(LocalDateTime.now()); record.setUpdatedAt(LocalDateTime.now()); record.setDeletedFlag(0); record.setCreatedBy(user.getUserId()); record.setUpdatedBy(user.getUserId()); record.setRemark(request.remark); inoutMapper.insert(record); return record;
    }

    private void fillMaterial(MaterialInfo m, MaterialRequest r, CurrentUser u, boolean create) { m.setMaterialCode(r.materialCode); m.setMaterialName(r.materialName); m.setMaterialCategory(r.materialCategory); m.setMaterialSpec(r.materialSpec); m.setMaterialModel(r.materialModel); m.setUnit(r.unit); m.setBrand(r.brand); m.setManufacturer(r.manufacturer); m.setSafetyStockQty(r.safetyStockQty); m.setEnabledFlag(r.enabledFlag == null ? 1 : r.enabledFlag); m.setTraceEnabled(r.traceEnabled == null ? 1 : r.traceEnabled); m.setQrcodeRequired(r.qrcodeRequired == null ? 0 : r.qrcodeRequired); m.setUpdatedAt(LocalDateTime.now()); m.setUpdatedBy(u.getUserId()); m.setRemark(r.remark); if (create) { m.setCreatedAt(LocalDateTime.now()); m.setDeletedFlag(0); m.setCreatedBy(u.getUserId()); } }
    private MaterialInfo requireMaterial(Long id) { MaterialInfo m = materialInfoMapper.selectById(id); if (m == null) throw new BusinessException(ErrorCode.NOT_FOUND, "物料不存在"); return m; }
    private void requireAdmin(CurrentUser user) { if (!dataScopeService.canAccessAll(user) && !user.getRoleCodes().contains("MATERIAL_MANAGER") && !"MATERIAL".equals(user.getDataScope())) throw new BusinessException(ErrorCode.FORBIDDEN, "无权访问物料管理"); }
    private WorkOrder requireAdminWorkOrder(Long id, CurrentUser user) { WorkOrder o=requireOrder(id); if (!dataScopeService.canAccessAll(user) && !dataScopeService.canAccessProject(user, o.getProjectId())) throw new BusinessException(ErrorCode.FORBIDDEN, "无权访问工单物料"); return o; }
    private WorkOrder requireMobileWorkOrder(Long id, CurrentUser user) { WorkOrder o=requireOrder(id); if (!(dataScopeService.canAccessAll(user)||dataScopeService.canAccessProject(user,o.getProjectId())||user.getUserId().equals(o.getMaintainerId())||assignmentMapper.selectByWorkOrderId(o.getId()).stream().map(WorkOrderAssignment::getAssigneeId).anyMatch(user.getUserId()::equals))) throw new BusinessException(ErrorCode.FORBIDDEN, "无权访问工单物料"); return o; }
    private WorkOrder requireOrder(Long id) { WorkOrder o=workOrderMapper.selectById(id); if (o==null) throw new BusinessException(ErrorCode.NOT_FOUND, "工单不存在"); return o; }
    private BigDecimal nvl(BigDecimal v) { return v == null ? BigDecimal.ZERO : v; }
    private String defaultText(String v, String d) { return StringUtils.hasText(v) ? v : d; }
    private String nowNo() { return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + UUID.randomUUID().toString().substring(0,4).toUpperCase(); }
    private MaterialVO toMaterialVO(MaterialInfo m) { MaterialVO v=new MaterialVO(); v.id=m.getId(); v.materialCode=m.getMaterialCode(); v.materialName=m.getMaterialName(); v.materialCategory=m.getMaterialCategory(); v.materialSpec=m.getMaterialSpec(); v.unit=m.getUnit(); v.safetyStockQty=m.getSafetyStockQty(); v.enabledFlag=m.getEnabledFlag(); v.traceEnabled=m.getTraceEnabled(); v.qrcodeRequired=m.getQrcodeRequired(); v.updatedAt=m.getUpdatedAt(); return v; }
    private InventoryVO toInventoryVO(MaterialInventory i) { InventoryVO v=new InventoryVO(); v.id=i.getId(); v.materialId=i.getMaterialId(); v.materialCode=i.getMaterialCode(); v.warehouseCode=i.getWarehouseCode(); v.warehouseName=i.getWarehouseName(); v.batchNo=i.getBatchNo(); v.currentQty=i.getCurrentQty(); v.availableQty=i.getAvailableQty(); v.inventoryStatus=i.getInventoryStatus(); return v; }
    private MaterialInoutVO toInoutVO(MaterialInoutRecord r) { MaterialInoutVO v=new MaterialInoutVO(); v.id=r.getId(); v.recordNo=r.getRecordNo(); v.materialId=r.getMaterialId(); v.materialCode=r.getMaterialCode(); v.materialName=r.getMaterialName(); v.inoutType=r.getInoutType(); v.quantity=r.getQuantity(); v.beforeQty=r.getBeforeQty(); v.afterQty=r.getAfterQty(); return v; }
    private MaterialQrcodeVO toQrcodeVO(MaterialQrcode q) { MaterialQrcodeVO v=new MaterialQrcodeVO(); v.id=q.getId(); v.materialId=q.getMaterialId(); v.materialCode=q.getMaterialCode(); v.qrcodeValue=q.getQrcodeValue(); v.qrcodeFileId=q.getQrcodeFileId(); v.batchNo=q.getBatchNo(); v.serialNo=q.getSerialNo(); v.qrcodeStatus=q.getQrcodeStatus(); return v; }
    private MaterialUsageVO toUsageVO(WorkOrderMaterialUsage u) { MaterialUsageVO v=new MaterialUsageVO(); v.id=u.getId(); v.serverId=u.getServerId(); v.localId=u.getLocalId(); v.usageNo=u.getUsageNo(); v.workOrderId=u.getWorkOrderId(); v.materialId=u.getMaterialId(); v.materialCode=u.getMaterialCode(); v.materialName=u.getMaterialName(); v.usedQty=u.getUsedQty(); v.wasteQty=u.getWasteQty(); v.returnQty=u.getReturnQty(); v.usageTime=u.getUsageTime(); v.version=u.getVersion(); v.updatedAt=u.getUpdatedAt(); v.syncStatus=u.getSyncStatus(); return v; }
    private MobileMaterialVO toMobileMaterialVO(WorkOrderMaterial m) { MobileMaterialVO v=new MobileMaterialVO(); v.id=m.getId(); v.serverId=m.getServerId(); v.localId=m.getLocalId(); v.workOrderId=m.getWorkOrderId(); v.materialCode=m.getMaterialCode(); v.materialName=m.getMaterialName(); v.materialSpec=m.getMaterialSpec(); v.unit=m.getUnit(); v.plannedQty=m.getPlannedQty(); v.actualQty=m.getActualQty(); v.prepareStatus=m.getPrepareStatus(); v.version=m.getVersion(); v.updatedAt=m.getUpdatedAt(); v.syncStatus=m.getSyncStatus(); return v; }
    private void writeLog(CurrentUser user, HttpServletRequest request, String type, String businessType, Long id, String no, Long projectId) { OperationLog log=new OperationLog(); log.setTraceId(TraceIdUtils.currentTraceId()); log.setOperatorId(user.getUserId()); log.setOperatorName(user.getRealName()); log.setRoleCode(String.join(",", user.getRoleCodes())); log.setPlatform(request.getRequestURI().contains("/mobile/") ? "MOBILE" : "PC"); log.setModuleName("MATERIAL"); log.setOperationType(type); log.setBusinessType(businessType); log.setBusinessId(String.valueOf(id)); log.setBusinessNo(no); log.setProjectId(projectId); log.setRequestMethod(request.getMethod()); log.setRequestPath(request.getRequestURI()); log.setRequestIp(request.getRemoteAddr()); log.setUserAgent(request.getHeader("User-Agent")); log.setResultStatus("SUCCESS"); log.setOperationTime(LocalDateTime.now()); log.setDeletedFlag(0); operationLogMapper.insert(log); }
}
