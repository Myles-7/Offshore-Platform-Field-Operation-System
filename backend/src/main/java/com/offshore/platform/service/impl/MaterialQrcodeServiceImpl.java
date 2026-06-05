package com.offshore.platform.service.impl;

import com.offshore.platform.entity.MaterialQrcode;
import com.offshore.platform.mapper.MaterialQrcodeMapper;
import com.offshore.platform.service.MaterialQrcodeService;
import java.util.List;

import org.springframework.stereotype.Service;

/**
 * material_qrcode 基础Service实现。
 */
@Service
public class MaterialQrcodeServiceImpl implements MaterialQrcodeService {
    private final MaterialQrcodeMapper materialQrcodeMapper;

    public MaterialQrcodeServiceImpl(MaterialQrcodeMapper materialQrcodeMapper) {
        this.materialQrcodeMapper = materialQrcodeMapper;
    }

    @Override
    public int create(MaterialQrcode materialQrcode) {
        return materialQrcodeMapper.insert(materialQrcode);
    }

    @Override
    public int update(MaterialQrcode materialQrcode) {
        return materialQrcodeMapper.updateById(materialQrcode);
    }

    @Override
    public MaterialQrcode getById(Long id) {
        return materialQrcodeMapper.selectById(id);
    }

    @Override
    public List<MaterialQrcode> listAll() {
        return materialQrcodeMapper.selectAll();
    }

    @Override
    public int removeById(Long id) {
        return materialQrcodeMapper.softDeleteById(id);
    }
}
