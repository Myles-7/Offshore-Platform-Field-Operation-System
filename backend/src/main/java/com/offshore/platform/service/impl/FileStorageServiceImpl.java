package com.offshore.platform.service.impl;

import com.offshore.platform.entity.FileStorage;
import com.offshore.platform.mapper.FileStorageMapper;
import com.offshore.platform.service.FileStorageService;
import java.util.List;

import org.springframework.stereotype.Service;

/**
 * file_storage 基础Service实现。
 */
@Service
public class FileStorageServiceImpl implements FileStorageService {
    private final FileStorageMapper fileStorageMapper;

    public FileStorageServiceImpl(FileStorageMapper fileStorageMapper) {
        this.fileStorageMapper = fileStorageMapper;
    }

    @Override
    public int create(FileStorage fileStorage) {
        return fileStorageMapper.insert(fileStorage);
    }

    @Override
    public int update(FileStorage fileStorage) {
        return fileStorageMapper.updateById(fileStorage);
    }

    @Override
    public FileStorage getById(Long id) {
        return fileStorageMapper.selectById(id);
    }

    @Override
    public List<FileStorage> listAll() {
        return fileStorageMapper.selectAll();
    }

    @Override
    public int removeById(Long id) {
        return fileStorageMapper.softDeleteById(id);
    }
}
