package com.offshore.platform.mapper;

import com.offshore.platform.entity.MaterialQrcode;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * material_qrcode 基础Mapper。
 */
public interface MaterialQrcodeMapper {
    int insert(MaterialQrcode materialQrcode);

    int updateById(MaterialQrcode materialQrcode);

    MaterialQrcode selectById(Long id);

    MaterialQrcode selectByQrcodeValue(@Param("qrcodeValue") String qrcodeValue);

    List<MaterialQrcode> selectAll();

    int softDeleteById(Long id);
}
