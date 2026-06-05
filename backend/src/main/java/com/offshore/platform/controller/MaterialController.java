package com.offshore.platform.controller;

import com.offshore.platform.common.response.ApiResponse;
import com.offshore.platform.common.log.OperationLog;
import com.offshore.platform.dto.material.MaterialInoutRequest;
import com.offshore.platform.dto.material.MaterialQrcodeRequest;
import com.offshore.platform.dto.material.MaterialRequest;
import com.offshore.platform.dto.material.MaterialUsageRequest;
import com.offshore.platform.service.MaterialService;
import com.offshore.platform.vo.material.InventoryVO;
import com.offshore.platform.vo.material.MaterialInoutVO;
import com.offshore.platform.vo.material.MaterialQrcodeVO;
import com.offshore.platform.vo.material.MaterialUsageVO;
import com.offshore.platform.vo.material.MaterialVO;
import com.offshore.platform.vo.mobile.MobileMaterialVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "material", description = "物料追溯")
@RestController
public class MaterialController {
    private final MaterialService materialService;

    public MaterialController(MaterialService materialService) {
        this.materialService = materialService;
    }

    @Operation(summary = "PC物料列表")
    @GetMapping("/api/admin/materials")
    public ApiResponse<List<MaterialVO>> listMaterials() { return ApiResponse.success(materialService.listMaterials()); }

    @Operation(summary = "PC物料详情")
    @GetMapping("/api/admin/materials/{id}")
    public ApiResponse<MaterialVO> getMaterial(@PathVariable Long id) { return ApiResponse.success(materialService.getMaterial(id)); }

    @Operation(summary = "PC新增物料")
    @PostMapping("/api/admin/materials")
    public ApiResponse<MaterialVO> createMaterial(@Valid @RequestBody MaterialRequest request, HttpServletRequest servletRequest) {
        return ApiResponse.success(materialService.createMaterial(request, servletRequest));
    }

    @Operation(summary = "PC修改物料")
    @PutMapping("/api/admin/materials/{id}")
    public ApiResponse<MaterialVO> updateMaterial(@PathVariable Long id, @Valid @RequestBody MaterialRequest request, HttpServletRequest servletRequest) {
        return ApiResponse.success(materialService.updateMaterial(id, request, servletRequest));
    }

    @Operation(summary = "PC删除物料")
    @DeleteMapping("/api/admin/materials/{id}")
    public ApiResponse<Void> deleteMaterial(@PathVariable Long id, HttpServletRequest servletRequest) {
        materialService.deleteMaterial(id, servletRequest);
        return ApiResponse.success();
    }

    @Operation(summary = "PC物料库存")
    @GetMapping("/api/admin/materials/{id}/inventory")
    public ApiResponse<List<InventoryVO>> listInventory(@PathVariable Long id) { return ApiResponse.success(materialService.listInventory(id)); }

    @Operation(summary = "PC物料入库")
    @PostMapping("/api/admin/materials/inbound")
    @OperationLog(module = "MATERIAL", operation = "MATERIAL_INBOUND", businessType = "MATERIAL")
    public ApiResponse<MaterialInoutVO> inbound(@Valid @RequestBody MaterialInoutRequest request, HttpServletRequest servletRequest) {
        return ApiResponse.success(materialService.inbound(request, servletRequest));
    }

    @Operation(summary = "PC物料出库")
    @PostMapping("/api/admin/materials/outbound")
    @OperationLog(module = "MATERIAL", operation = "MATERIAL_OUTBOUND", businessType = "MATERIAL")
    public ApiResponse<MaterialInoutVO> outbound(@Valid @RequestBody MaterialInoutRequest request, HttpServletRequest servletRequest) {
        return ApiResponse.success(materialService.outbound(request, servletRequest));
    }

    @Operation(summary = "PC库存盘点")
    @PostMapping("/api/admin/materials/stocktaking")
    @OperationLog(module = "MATERIAL", operation = "MATERIAL_STOCKTAKING", businessType = "MATERIAL")
    public ApiResponse<MaterialInoutVO> stocktaking(@Valid @RequestBody MaterialInoutRequest request, HttpServletRequest servletRequest) {
        return ApiResponse.success(materialService.stocktaking(request, servletRequest));
    }

    @Operation(summary = "PC生成物料二维码")
    @PostMapping("/api/admin/materials/{id}/qrcode")
    public ApiResponse<MaterialQrcodeVO> createQrcode(@PathVariable Long id, @RequestBody MaterialQrcodeRequest request, HttpServletRequest servletRequest) {
        return ApiResponse.success(materialService.createQrcode(id, request, servletRequest));
    }

    @Operation(summary = "PC扫描物料二维码")
    @GetMapping("/api/admin/materials/qrcode/{code}")
    public ApiResponse<MaterialQrcodeVO> getQrcode(@PathVariable String code) { return ApiResponse.success(materialService.getQrcode(code)); }

    @Operation(summary = "PC工单物料使用")
    @GetMapping("/api/admin/work-orders/{workOrderId}/material-usage")
    public ApiResponse<List<MaterialUsageVO>> listUsage(@PathVariable Long workOrderId) {
        return ApiResponse.success(materialService.listWorkOrderUsage(workOrderId));
    }

    @Operation(summary = "移动端工单物料需求")
    @GetMapping("/api/mobile/work-orders/{workOrderId}/material-requirements")
    public ApiResponse<List<MobileMaterialVO>> listMobileRequirements(@PathVariable Long workOrderId) {
        return ApiResponse.success(materialService.listMobileRequirements(workOrderId));
    }

    @Operation(summary = "移动端记录物料使用")
    @PostMapping("/api/mobile/work-orders/{workOrderId}/material-usage")
    public ApiResponse<MaterialUsageVO> createMobileUsage(@PathVariable Long workOrderId,
            @Valid @RequestBody MaterialUsageRequest request, HttpServletRequest servletRequest) {
        return ApiResponse.success(materialService.createMobileUsage(workOrderId, request, servletRequest));
    }
}
