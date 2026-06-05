package com.offshore.platform.mapper;

import com.offshore.platform.entity.FileStorage;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * file_storage 基础Mapper。
 */
public interface FileStorageMapper {
    int insert(FileStorage fileStorage);

    int updateById(FileStorage fileStorage);

    FileStorage selectById(Long id);

    FileStorage selectByFileId(@Param("fileId") String fileId);

    List<FileStorage> selectAll();

    int softDeleteById(Long id);
}
