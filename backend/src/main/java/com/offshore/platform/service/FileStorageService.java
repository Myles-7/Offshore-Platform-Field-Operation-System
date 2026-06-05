package com.offshore.platform.service;

import com.offshore.platform.entity.FileStorage;
import java.util.List;

/**
 * file_storage 基础Service。
 */
public interface FileStorageService {
    int create(FileStorage fileStorage);

    int update(FileStorage fileStorage);

    FileStorage getById(Long id);

    List<FileStorage> listAll();

    int removeById(Long id);
}
