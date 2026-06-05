package com.offshore.platform.service;

import com.offshore.platform.dto.material.MaterialInoutRequest;
import com.offshore.platform.dto.material.MaterialQrcodeRequest;
import com.offshore.platform.dto.material.MaterialRequest;
import com.offshore.platform.dto.material.MaterialUsageRequest;
import com.offshore.platform.vo.material.InventoryVO;
import com.offshore.platform.vo.material.MaterialInoutVO;
import com.offshore.platform.vo.material.MaterialQrcodeVO;
import com.offshore.platform.vo.material.MaterialUsageVO;
import com.offshore.platform.vo.material.MaterialVO;
import com.offshore.platform.vo.mobile.MobileMaterialVO;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

public interface MaterialService {
    List<MaterialVO> listMaterials();
    MaterialVO getMaterial(Long id);
    MaterialVO createMaterial(MaterialRequest request, HttpServletRequest servletRequest);
    MaterialVO updateMaterial(Long id, MaterialRequest request, HttpServletRequest servletRequest);
    void deleteMaterial(Long id, HttpServletRequest servletRequest);
    List<InventoryVO> listInventory(Long materialId);
    MaterialInoutVO inbound(MaterialInoutRequest request, HttpServletRequest servletRequest);
    MaterialInoutVO outbound(MaterialInoutRequest request, HttpServletRequest servletRequest);
    MaterialInoutVO stocktaking(MaterialInoutRequest request, HttpServletRequest servletRequest);
    MaterialQrcodeVO createQrcode(Long materialId, MaterialQrcodeRequest request, HttpServletRequest servletRequest);
    MaterialQrcodeVO getQrcode(String code);
    List<MaterialUsageVO> listWorkOrderUsage(Long workOrderId);
    List<MobileMaterialVO> listMobileRequirements(Long workOrderId);
    MaterialUsageVO createMobileUsage(Long workOrderId, MaterialUsageRequest request, HttpServletRequest servletRequest);
}
