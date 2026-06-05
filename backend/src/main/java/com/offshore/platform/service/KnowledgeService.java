package com.offshore.platform.service;

import com.offshore.platform.entity.KnowledgeCase;
import com.offshore.platform.entity.MaintenanceProcess;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

public interface KnowledgeService {
    List<KnowledgeCase> listCases();
    KnowledgeCase getCase(Long id);
    KnowledgeCase createCase(KnowledgeCase entity, HttpServletRequest request);
    KnowledgeCase updateCase(Long id, KnowledgeCase entity, HttpServletRequest request);
    void deleteCase(Long id, HttpServletRequest request);

    List<MaintenanceProcess> listProcesses();
    MaintenanceProcess getProcess(Long id);
    MaintenanceProcess createProcess(MaintenanceProcess entity, HttpServletRequest request);
    MaintenanceProcess updateProcess(Long id, MaintenanceProcess entity, HttpServletRequest request);
    void deleteProcess(Long id, HttpServletRequest request);
}
