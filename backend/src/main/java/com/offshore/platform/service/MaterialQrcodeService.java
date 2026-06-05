package com.offshore.platform.service;

import com.offshore.platform.entity.MaterialQrcode;
import java.util.List;

/**
 * material_qrcode 基础Service。
 */
public interface MaterialQrcodeService {
    int create(MaterialQrcode materialQrcode);

    int update(MaterialQrcode materialQrcode);

    MaterialQrcode getById(Long id);

    List<MaterialQrcode> listAll();

    int removeById(Long id);
}
