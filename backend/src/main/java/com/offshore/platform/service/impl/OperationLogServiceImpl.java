package com.offshore.platform.service.impl;

import com.offshore.platform.entity.OperationLog;
import com.offshore.platform.mapper.OperationLogMapper;
import com.offshore.platform.service.OperationLogService;
import java.util.List;

import org.springframework.stereotype.Service;

/**
 * operation_log 基础Service实现。
 */
@Service
public class OperationLogServiceImpl implements OperationLogService {
    private final OperationLogMapper operationLogMapper;

    public OperationLogServiceImpl(OperationLogMapper operationLogMapper) {
        this.operationLogMapper = operationLogMapper;
    }

    @Override
    public int create(OperationLog operationLog) {
        return operationLogMapper.insert(operationLog);
    }

    @Override
    public int update(OperationLog operationLog) {
        return operationLogMapper.updateById(operationLog);
    }

    @Override
    public OperationLog getById(Long id) {
        return operationLogMapper.selectById(id);
    }

    @Override
    public List<OperationLog> listAll() {
        return operationLogMapper.selectAll();
    }

    @Override
    public int removeById(Long id) {
        return operationLogMapper.softDeleteById(id);
    }
}
