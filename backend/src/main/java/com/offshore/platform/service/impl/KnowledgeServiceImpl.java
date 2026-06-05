package com.offshore.platform.service.impl;

import com.offshore.platform.common.context.CurrentUser;
import com.offshore.platform.common.context.CurrentUserContext;
import com.offshore.platform.common.enums.ErrorCode;
import com.offshore.platform.common.exception.BusinessException;
import com.offshore.platform.entity.KnowledgeCase;
import com.offshore.platform.entity.MaintenanceProcess;
import com.offshore.platform.mapper.KnowledgeCaseMapper;
import com.offshore.platform.mapper.MaintenanceProcessMapper;
import com.offshore.platform.mapper.OperationLogMapper;
import com.offshore.platform.entity.OperationLog;
import com.offshore.platform.service.KnowledgeService;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class KnowledgeServiceImpl implements KnowledgeService {
    private final KnowledgeCaseMapper caseMapper;
    private final MaintenanceProcessMapper processMapper;
    private final OperationLogMapper operationLogMapper;

    public KnowledgeServiceImpl(KnowledgeCaseMapper caseMapper, MaintenanceProcessMapper processMapper,
            OperationLogMapper operationLogMapper) {
        this.caseMapper = caseMapper;
        this.processMapper = processMapper;
        this.operationLogMapper = operationLogMapper;
    }

    private CurrentUser requireUser() { return CurrentUserContext.require(); }

    /* ========== 故障案例 ========== */
    @Override
    public List<KnowledgeCase> listCases() { requireUser(); return caseMapper.selectAll(); }

    @Override
    public KnowledgeCase getCase(Long id) {
        requireUser();
        KnowledgeCase entity = caseMapper.selectById(id);
        if (entity == null) throw new BusinessException(ErrorCode.NOT_FOUND, "故障案例不存在");
        return entity;
    }

    @Override
    @Transactional
    public KnowledgeCase createCase(KnowledgeCase entity, HttpServletRequest request) {
        CurrentUser user = requireUser();
        LocalDateTime now = LocalDateTime.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        entity.setCreatedBy(user.getUserId());
        entity.setUpdatedBy(user.getUserId());
        entity.setVersion(1);
        entity.setSyncStatus("SYNCED");
        entity.setDeletedFlag(0);
        caseMapper.insert(entity);
        writeLog(user, request, "CREATE", "KNOWLEDGE_CASE", entity.getId(), entity.getCaseNo());
        return entity;
    }

    @Override
    @Transactional
    public KnowledgeCase updateCase(Long id, KnowledgeCase entity, HttpServletRequest request) {
        CurrentUser user = requireUser();
        KnowledgeCase existing = caseMapper.selectById(id);
        if (existing == null) throw new BusinessException(ErrorCode.NOT_FOUND, "故障案例不存在");
        entity.setId(id);
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setUpdatedBy(user.getUserId());
        entity.setVersion(existing.getVersion() == null ? 1 : existing.getVersion() + 1);
        caseMapper.updateById(entity);
        writeLog(user, request, "UPDATE", "KNOWLEDGE_CASE", id, existing.getCaseNo());
        return caseMapper.selectById(id);
    }

    @Override
    @Transactional
    public void deleteCase(Long id, HttpServletRequest request) {
        CurrentUser user = requireUser();
        KnowledgeCase existing = caseMapper.selectById(id);
        if (existing == null) throw new BusinessException(ErrorCode.NOT_FOUND, "故障案例不存在");
        caseMapper.softDeleteById(id);
        writeLog(user, request, "DELETE", "KNOWLEDGE_CASE", id, existing.getCaseNo());
    }

    /* ========== 维修工艺 ========== */
    @Override
    public List<MaintenanceProcess> listProcesses() { requireUser(); return processMapper.selectAll(); }

    @Override
    public MaintenanceProcess getProcess(Long id) {
        requireUser();
        MaintenanceProcess entity = processMapper.selectById(id);
        if (entity == null) throw new BusinessException(ErrorCode.NOT_FOUND, "维修工艺不存在");
        return entity;
    }

    @Override
    @Transactional
    public MaintenanceProcess createProcess(MaintenanceProcess entity, HttpServletRequest request) {
        CurrentUser user = requireUser();
        LocalDateTime now = LocalDateTime.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        entity.setCreatedBy(user.getUserId());
        entity.setUpdatedBy(user.getUserId());
        entity.setVersion(1);
        entity.setSyncStatus("SYNCED");
        entity.setDeletedFlag(0);
        processMapper.insert(entity);
        writeLog(user, request, "CREATE", "MAINTENANCE_PROCESS", entity.getId(), entity.getProcessCode());
        return entity;
    }

    @Override
    @Transactional
    public MaintenanceProcess updateProcess(Long id, MaintenanceProcess entity, HttpServletRequest request) {
        CurrentUser user = requireUser();
        MaintenanceProcess existing = processMapper.selectById(id);
        if (existing == null) throw new BusinessException(ErrorCode.NOT_FOUND, "维修工艺不存在");
        entity.setId(id);
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setUpdatedBy(user.getUserId());
        entity.setVersion(existing.getVersion() == null ? 1 : existing.getVersion() + 1);
        processMapper.updateById(entity);
        writeLog(user, request, "UPDATE", "MAINTENANCE_PROCESS", id, existing.getProcessCode());
        return processMapper.selectById(id);
    }

    @Override
    @Transactional
    public void deleteProcess(Long id, HttpServletRequest request) {
        CurrentUser user = requireUser();
        MaintenanceProcess existing = processMapper.selectById(id);
        if (existing == null) throw new BusinessException(ErrorCode.NOT_FOUND, "维修工艺不存在");
        processMapper.softDeleteById(id);
        writeLog(user, request, "DELETE", "MAINTENANCE_PROCESS", id, existing.getProcessCode());
    }

    private void writeLog(CurrentUser user, HttpServletRequest request, String opType, String bizType, Long bizId, String bizNo) {
        OperationLog log = new OperationLog();
        log.setOperatorId(user.getUserId());
        log.setOperatorName(user.getRealName());
        log.setRoleCode(String.join(",", user.getRoleCodes()));
        log.setPlatform("PC");
        log.setModuleName("KNOWLEDGE");
        log.setOperationType(opType);
        log.setBusinessType(bizType);
        log.setBusinessId(String.valueOf(bizId));
        log.setBusinessNo(bizNo);
        log.setRequestMethod(request.getMethod());
        log.setRequestPath(request.getRequestURI());
        log.setRequestIp(clientIp(request));
        log.setUserAgent(request.getHeader("User-Agent"));
        log.setResultStatus("SUCCESS");
        log.setOperationTime(LocalDateTime.now());
        log.setDeletedFlag(0);
        log.setCreatedBy(user.getUserId());
        log.setUpdatedBy(user.getUserId());
        operationLogMapper.insert(log);
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        return (forwarded != null && !forwarded.isEmpty()) ? forwarded.split(",")[0].trim() : request.getRemoteAddr();
    }
}
