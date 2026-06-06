package com.offshore.platform.service.impl;

import com.offshore.platform.common.context.CurrentUser;
import com.offshore.platform.common.context.CurrentUserContext;
import com.offshore.platform.common.enums.ErrorCode;
import com.offshore.platform.common.exception.BusinessException;
import com.offshore.platform.common.util.TraceIdUtils;
import com.offshore.platform.dto.acceptance.AcceptanceRequest;
import com.offshore.platform.dto.acceptance.AcceptanceLockRequest;
import com.offshore.platform.dto.acceptance.AcceptanceRelockRequest;
import com.offshore.platform.dto.acceptance.AcceptanceReviewRequest;
import com.offshore.platform.dto.acceptance.AcceptanceUnlockRequest;
import com.offshore.platform.dto.acceptance.PdfGenerateRequest;
import com.offshore.platform.dto.acceptance.PdfMetadataRequest;
import com.offshore.platform.dto.acceptance.PdfVoidRequest;
import com.offshore.platform.dto.acceptance.SignatureRequest;
import com.offshore.platform.entity.FileStorage;
import com.offshore.platform.entity.OperationLog;
import com.offshore.platform.entity.ProjectInfo;
import com.offshore.platform.entity.WorkOrder;
import com.offshore.platform.entity.WorkOrderAcceptance;
import com.offshore.platform.entity.WorkOrderAssignment;
import com.offshore.platform.entity.WorkOrderPdf;
import com.offshore.platform.entity.WorkOrderSignature;
import com.offshore.platform.entity.WorkOrderStatusLog;
import com.offshore.platform.entity.WorkOrderVersionLog;
import com.offshore.platform.mapper.FileStorageMapper;
import com.offshore.platform.mapper.OperationLogMapper;
import com.offshore.platform.mapper.ProjectInfoMapper;
import com.offshore.platform.mapper.WorkOrderAcceptanceMapper;
import com.offshore.platform.mapper.WorkOrderAssignmentMapper;
import com.offshore.platform.mapper.WorkOrderMapper;
import com.offshore.platform.mapper.WorkOrderPdfMapper;
import com.offshore.platform.mapper.WorkOrderSignatureMapper;
import com.offshore.platform.mapper.WorkOrderStatusLogMapper;
import com.offshore.platform.mapper.WorkOrderVersionLogMapper;
import com.offshore.platform.service.AcceptanceService;
import com.offshore.platform.service.DataScopeService;
import com.offshore.platform.vo.acceptance.AcceptanceVO;
import com.offshore.platform.vo.acceptance.AcceptanceLockHistoryVO;
import com.offshore.platform.vo.acceptance.AcceptanceLockStatusVO;
import com.offshore.platform.vo.acceptance.PdfVO;
import com.offshore.platform.vo.acceptance.SignatureVO;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AcceptanceServiceImpl implements AcceptanceService {
    private final WorkOrderMapper workOrderMapper;
    private final ProjectInfoMapper projectInfoMapper;
    private final WorkOrderAssignmentMapper assignmentMapper;
    private final WorkOrderAcceptanceMapper acceptanceMapper;
    private final WorkOrderSignatureMapper signatureMapper;
    private final WorkOrderPdfMapper pdfMapper;
    private final WorkOrderStatusLogMapper statusLogMapper;
    private final WorkOrderVersionLogMapper versionLogMapper;
    private final FileStorageMapper fileStorageMapper;
    private final OperationLogMapper operationLogMapper;
    private final DataScopeService dataScopeService;
    private final Path storageRoot;

    public AcceptanceServiceImpl(WorkOrderMapper workOrderMapper, ProjectInfoMapper projectInfoMapper,
            WorkOrderAssignmentMapper assignmentMapper, WorkOrderAcceptanceMapper acceptanceMapper,
            WorkOrderSignatureMapper signatureMapper, WorkOrderPdfMapper pdfMapper,
            WorkOrderStatusLogMapper statusLogMapper, WorkOrderVersionLogMapper versionLogMapper, FileStorageMapper fileStorageMapper,
            OperationLogMapper operationLogMapper, DataScopeService dataScopeService,
            @Value("${app.file.storage-root:uploads}") String storageRoot) {
        this.workOrderMapper = workOrderMapper;
        this.projectInfoMapper = projectInfoMapper;
        this.assignmentMapper = assignmentMapper;
        this.acceptanceMapper = acceptanceMapper;
        this.signatureMapper = signatureMapper;
        this.pdfMapper = pdfMapper;
        this.statusLogMapper = statusLogMapper;
        this.versionLogMapper = versionLogMapper;
        this.fileStorageMapper = fileStorageMapper;
        this.operationLogMapper = operationLogMapper;
        this.dataScopeService = dataScopeService;
        this.storageRoot = Paths.get(storageRoot).toAbsolutePath().normalize();
    }

    @Override
    @Transactional
    public SignatureVO createSignature(Long workOrderId, SignatureRequest request, HttpServletRequest servletRequest) {
        CurrentUser user = CurrentUserContext.require();
        WorkOrder order = requireMobileWorkOrder(workOrderId, user);
        return createSignature(order, request, user, servletRequest);
    }

    @Override
    public List<SignatureVO> listMobileSignatures(Long workOrderId) {
        requireMobileWorkOrder(workOrderId, CurrentUserContext.require());
        return signatureMapper.selectByWorkOrderId(workOrderId).stream().map(this::toSignatureVO).toList();
    }

    @Override
    @Transactional
    public AcceptanceVO submitAcceptance(Long workOrderId, AcceptanceRequest request, HttpServletRequest servletRequest) {
        CurrentUser user = CurrentUserContext.require();
        WorkOrder order = requireMobileWorkOrder(workOrderId, user);
        WorkOrderAcceptance existingLocal = findAcceptanceByLocalId(workOrderId, request.localId);
        if (existingLocal != null) {
            return toAcceptanceVO(existingLocal);
        }
        WorkOrderAcceptance current = latestAcceptance(workOrderId);
        if (current != null && Integer.valueOf(1).equals(current.getLockedFlag())) {
            throw new BusinessException(ErrorCode.SYNC_ERROR, "Acceptance is locked");
        }
        return saveAcceptance(order, current == null ? new WorkOrderAcceptance() : current, request, user, servletRequest,
                current == null ? "SUBMIT_ACCEPTANCE" : "UPDATE_ACCEPTANCE");
    }

    @Override
    @Transactional
    public AcceptanceVO updateAcceptance(Long workOrderId, Long acceptanceId, AcceptanceRequest request, HttpServletRequest servletRequest) {
        CurrentUser user = CurrentUserContext.require();
        WorkOrder order = requireMobileWorkOrder(workOrderId, user);
        WorkOrderAcceptance acceptance = requireAcceptance(workOrderId, acceptanceId);
        if (Integer.valueOf(1).equals(acceptance.getLockedFlag())) {
            throw new BusinessException(ErrorCode.SYNC_ERROR, "Acceptance is locked");
        }
        if (request.version != null && !request.version.equals(acceptance.getVersion())) {
            throw new BusinessException(ErrorCode.SYNC_ERROR, "Acceptance version conflict");
        }
        return saveAcceptance(order, acceptance, request, user, servletRequest, "UPDATE_ACCEPTANCE");
    }

    @Override
    @Transactional
    public PdfVO savePdfMetadata(Long workOrderId, PdfMetadataRequest request, HttpServletRequest servletRequest) {
        CurrentUser user = CurrentUserContext.require();
        WorkOrder order = requireMobileWorkOrder(workOrderId, user);
        if (fileStorageMapper.selectByFileId(request.fileId) == null) {
            throw new BusinessException(ErrorCode.FILE_ERROR, "PDF file does not exist");
        }
        WorkOrderPdf existing = findPdfByLocalId(workOrderId, request.localId);
        if (existing != null) {
            return toPdfVO(existing);
        }
        WorkOrderAcceptance acceptance = request.acceptanceId == null ? latestAcceptance(workOrderId) : requireAcceptance(workOrderId, request.acceptanceId);
        WorkOrderPdf pdf = buildPdf(order, acceptance, user, request.fileId, request.pdfContentSnapshot, request.localId, request.deviceId);
        if (StringUtils.hasText(request.pdfNo)) {
            pdf.setPdfNo(request.pdfNo);
        }
        if (StringUtils.hasText(request.pdfStatus)) {
            pdf.setPdfStatus(request.pdfStatus);
            pdf.setArchiveStatus("ARCHIVED".equals(request.pdfStatus) ? "ARCHIVED" : "NONE");
        }
        pdfMapper.insert(pdf);
        pdf.setServerId(pdf.getId());
        pdfMapper.updateById(pdf);
        lockAcceptance(acceptance, user, "PDF_METADATA_BOUND");
        writeLog(user, servletRequest, "SAVE_PDF_METADATA", "WORK_ORDER_PDF", pdf.getId(), order);
        return toPdfVO(pdf);
    }

    @Override
    public List<PdfVO> listMobilePdfs(Long workOrderId) {
        requireMobileWorkOrder(workOrderId, CurrentUserContext.require());
        return pdfMapper.selectByWorkOrderId(workOrderId).stream().map(this::toPdfVO).toList();
    }

    @Override
    public List<SignatureVO> listSignatures(Long workOrderId) {
        requireAdminWorkOrder(workOrderId, CurrentUserContext.require());
        return signatureMapper.selectByWorkOrderId(workOrderId).stream().map(this::toSignatureVO).toList();
    }

    @Override
    @Transactional
    public SignatureVO createAdminSignature(Long workOrderId, SignatureRequest request, HttpServletRequest servletRequest) {
        CurrentUser user = CurrentUserContext.require();
        WorkOrder order = requireAdminWorkOrder(workOrderId, user);
        return createSignature(order, request, user, servletRequest);
    }

    @Override
    public List<AcceptanceVO> listAcceptance(Long workOrderId) {
        requireAdminWorkOrder(workOrderId, CurrentUserContext.require());
        return acceptanceMapper.selectByWorkOrderId(workOrderId).stream().map(this::toAcceptanceVO).toList();
    }

    @Override
    @Transactional
    public AcceptanceVO reviewAcceptance(Long workOrderId, AcceptanceReviewRequest request, HttpServletRequest servletRequest) {
        CurrentUser user = CurrentUserContext.require();
        WorkOrder order = requireAdminWorkOrder(workOrderId, user);
        WorkOrderAcceptance acceptance = latestAcceptance(workOrderId);
        if (acceptance == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Acceptance not found");
        }
        if ("REJECTED".equals(request.acceptanceStatus) && !StringUtils.hasText(request.rejectReason)) {
            throw new BusinessException(ErrorCode.PDF_ERROR, "Reject reason is required");
        }
        acceptance.setAcceptanceUserId(user.getUserId());
        acceptance.setAcceptanceUserName(user.getRealName());
        acceptance.setAcceptanceTime(LocalDateTime.now());
        acceptance.setAcceptanceStatus(request.acceptanceStatus);
        acceptance.setAcceptanceResult(request.acceptanceResult);
        acceptance.setAcceptanceOpinion(request.acceptanceOpinion);
        acceptance.setProblemDesc("REJECTED".equals(request.acceptanceStatus) ? request.rejectReason : acceptance.getProblemDesc());
        acceptance.setVersion(defaultInt(acceptance.getVersion(), 1) + 1);
        acceptance.setUpdatedAt(LocalDateTime.now());
        acceptance.setUpdatedBy(user.getUserId());
        acceptanceMapper.updateById(acceptance);
        applyAcceptanceStatus(order, request.acceptanceStatus, user, null);
        writeLog(user, servletRequest, "REVIEW_ACCEPTANCE", "WORK_ORDER_ACCEPTANCE", acceptance.getId(), order);
        return toAcceptanceVO(acceptance);
    }

    @Override
    public PdfVO generatePdf(Long workOrderId, HttpServletRequest servletRequest) {
        return generatePdf(workOrderId, new PdfGenerateRequest(), servletRequest);
    }

    @Override
    @Transactional
    public PdfVO generatePdf(Long workOrderId, PdfGenerateRequest request, HttpServletRequest servletRequest) {
        CurrentUser user = CurrentUserContext.require();
        WorkOrder order = requireAdminWorkOrder(workOrderId, user);
        WorkOrderAcceptance acceptance = request.acceptanceId == null ? latestAcceptance(workOrderId) : requireAcceptance(workOrderId, request.acceptanceId);
        if (acceptance == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Acceptance not found");
        }
        try {
            String fileId = "FILE-" + nowNo() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            String fileName = "acceptance-" + order.getWorkOrderNo() + ".pdf";
            Path relative = Paths.get(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")), fileId + ".pdf");
            Path target = resolve(relative.toString());
            Files.createDirectories(target.getParent());
            byte[] bytes = simplePdf(order, acceptance);
            Files.write(target, bytes);
            String hash = sha256(bytes);
            FileStorage storage = new FileStorage();
            storage.setFileId(fileId);
            storage.setOriginalName(fileName);
            storage.setStoredName(fileId + ".pdf");
            storage.setFileType("PDF");
            storage.setMimeType("application/pdf");
            storage.setFileSize((long) bytes.length);
            storage.setStorageType("LOCAL");
            storage.setFilePath(relative.toString().replace('\\', '/'));
            storage.setPreviewPath(storage.getFilePath());
            storage.setFileHash(hash);
            storage.setChecksum(hash);
            storage.setUploadUserId(user.getUserId());
            storage.setUploadUserName(user.getRealName());
            storage.setUploadTime(LocalDateTime.now());
            storage.setUploadStatus("UPLOADED");
            storage.setRetryCount(0);
            storage.setWorkOrderId(workOrderId);
            storage.setAccessLevel("PRIVATE");
            storage.setPreviewEnabled(1);
            storage.setDownloadEnabled(1);
            storage.setCacheEnabled(0);
            storage.setVersion(1);
            storage.setSyncStatus("SYNCED");
            storage.setOperatorId(user.getUserId());
            storage.setConflictFlag(0);
            storage.setCreatedAt(LocalDateTime.now());
            storage.setUpdatedAt(LocalDateTime.now());
            storage.setDeletedFlag(0);
            storage.setCreatedBy(user.getUserId());
            storage.setUpdatedBy(user.getUserId());
            fileStorageMapper.insert(storage);
            storage.setServerId(storage.getId());
            fileStorageMapper.updateById(storage);

            WorkOrderPdf pdf = buildPdf(order, acceptance, user, fileId, new String(bytes, StandardCharsets.ISO_8859_1), null, null);
            if (StringUtils.hasText(request.pdfNo)) {
                pdf.setPdfNo(request.pdfNo);
            }
            pdfMapper.insert(pdf);
            pdf.setServerId(pdf.getId());
            pdfMapper.updateById(pdf);
            lockAcceptance(acceptance, user, "PDF_GENERATED");
            writeLog(user, servletRequest, "GENERATE_ACCEPTANCE_PDF", "WORK_ORDER_PDF", pdf.getId(), order);
            return toPdfVO(pdf);
        } catch (IOException ex) {
            throw new BusinessException(ErrorCode.PDF_ERROR, "PDF generation failed");
        }
    }

    @Override
    public List<PdfVO> listPdfs(Long workOrderId) {
        requireAdminWorkOrder(workOrderId, CurrentUserContext.require());
        return pdfMapper.selectByWorkOrderId(workOrderId).stream().map(this::toPdfVO).toList();
    }

    @Override
    public PdfVO latestPdf(Long workOrderId) {
        requireAdminWorkOrder(workOrderId, CurrentUserContext.require());
        return pdfMapper.selectByWorkOrderId(workOrderId).stream().findFirst().map(this::toPdfVO)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "PDF not found"));
    }

    @Override
    @Transactional
    public PdfVO archivePdf(Long workOrderId, HttpServletRequest servletRequest) {
        CurrentUser user = CurrentUserContext.require();
        WorkOrder order = requireAdminWorkOrder(workOrderId, user);
        WorkOrderPdf pdf = pdfMapper.selectByWorkOrderId(workOrderId).stream().findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "PDF not found"));
        pdf.setPdfStatus("ARCHIVED");
        pdf.setArchiveStatus("ARCHIVED");
        pdf.setLockedFlag(1);
        pdf.setVersion(defaultInt(pdf.getVersion(), 1) + 1);
        pdf.setUpdatedAt(LocalDateTime.now());
        pdf.setUpdatedBy(user.getUserId());
        pdfMapper.updateById(pdf);
        WorkOrderAcceptance acceptance = pdf.getAcceptanceId() == null ? latestAcceptance(workOrderId) : acceptanceMapper.selectById(pdf.getAcceptanceId());
        lockAcceptance(acceptance, user, "PDF_ARCHIVED");
        writeLog(user, servletRequest, "ARCHIVE_ACCEPTANCE_PDF", "WORK_ORDER_PDF", pdf.getId(), order);
        return toPdfVO(pdf);
    }

    @Override
    public AcceptanceLockStatusVO lockStatus(Long workOrderId) {
        requireAdminWorkOrder(workOrderId, CurrentUserContext.require());
        return toLockStatus(workOrderId);
    }

    @Override
    @Transactional
    public AcceptanceLockStatusVO lockAcceptance(Long workOrderId, AcceptanceLockRequest request, HttpServletRequest servletRequest) {
        CurrentUser user = CurrentUserContext.require();
        WorkOrder order = requireAdminWorkOrder(workOrderId, user);
        WorkOrderAcceptance acceptance = latestAcceptance(workOrderId);
        if (acceptance == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Acceptance not found");
        }
        acceptance.setLockedFlag(1);
        acceptance.setLockedAt(LocalDateTime.now());
        acceptance.setLockedBy(user.getUserId());
        acceptance.setLockReason(request.lockReason);
        acceptance.setVersion(defaultInt(acceptance.getVersion(), 1) + 1);
        acceptance.setUpdatedAt(LocalDateTime.now());
        acceptance.setUpdatedBy(user.getUserId());
        acceptanceMapper.updateById(acceptance);
        writeVersionLog(order, user, "LOCK_ACCEPTANCE", request.lockReason, acceptance.getVersion());
        writeLog(user, servletRequest, "LOCK_ACCEPTANCE", "WORK_ORDER_ACCEPTANCE", acceptance.getId(), order);
        return toLockStatus(workOrderId);
    }

    @Override
    @Transactional
    public AcceptanceLockStatusVO unlockAcceptance(Long workOrderId, AcceptanceUnlockRequest request, HttpServletRequest servletRequest) {
        CurrentUser user = CurrentUserContext.require();
        WorkOrder order = requireAdminWorkOrder(workOrderId, user);
        WorkOrderAcceptance acceptance = latestAcceptance(workOrderId);
        if (acceptance == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Acceptance not found");
        }
        acceptance.setLockedFlag(0);
        acceptance.setLockReason("UNLOCK:" + request.unlockReason + ";scope=" + defaultText(request.unlockScope, "ACCEPTANCE_ONLY"));
        acceptance.setVersion(defaultInt(acceptance.getVersion(), 1) + 1);
        acceptance.setUpdatedAt(LocalDateTime.now());
        acceptance.setUpdatedBy(user.getUserId());
        acceptanceMapper.updateById(acceptance);
        writeVersionLog(order, user, "UNLOCK_ACCEPTANCE", request.unlockReason, acceptance.getVersion());
        writeLog(user, servletRequest, "UNLOCK_ACCEPTANCE", "WORK_ORDER_ACCEPTANCE", acceptance.getId(), order);
        return toLockStatus(workOrderId);
    }

    @Override
    @Transactional
    public AcceptanceLockStatusVO relockAcceptance(Long workOrderId, AcceptanceRelockRequest request, HttpServletRequest servletRequest) {
        CurrentUser user = CurrentUserContext.require();
        WorkOrder order = requireAdminWorkOrder(workOrderId, user);
        WorkOrderAcceptance acceptance = latestAcceptance(workOrderId);
        if (acceptance == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Acceptance not found");
        }
        acceptance.setLockedFlag(1);
        acceptance.setLockedAt(LocalDateTime.now());
        acceptance.setLockedBy(user.getUserId());
        acceptance.setLockReason("RELOCK:" + request.relockReason);
        acceptance.setVersion(defaultInt(acceptance.getVersion(), 1) + 1);
        acceptance.setUpdatedAt(LocalDateTime.now());
        acceptance.setUpdatedBy(user.getUserId());
        acceptanceMapper.updateById(acceptance);
        writeVersionLog(order, user, "RELOCK_ACCEPTANCE", request.relockReason, acceptance.getVersion());
        writeLog(user, servletRequest, "RELOCK_ACCEPTANCE", "WORK_ORDER_ACCEPTANCE", acceptance.getId(), order);
        return toLockStatus(workOrderId);
    }

    @Override
    @Transactional
    public PdfVO voidPdf(Long workOrderId, Long pdfId, PdfVoidRequest request, HttpServletRequest servletRequest) {
        CurrentUser user = CurrentUserContext.require();
        WorkOrder order = requireAdminWorkOrder(workOrderId, user);
        WorkOrderPdf pdf = pdfMapper.selectById(pdfId);
        if (pdf == null || !workOrderId.equals(pdf.getWorkOrderId())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "PDF not found");
        }
        pdf.setPdfStatus("VOIDED");
        pdf.setArchiveStatus("ARCHIVED_INVALID");
        pdf.setRemark(request.voidReason);
        pdf.setVersion(defaultInt(pdf.getVersion(), 1) + 1);
        pdf.setUpdatedAt(LocalDateTime.now());
        pdf.setUpdatedBy(user.getUserId());
        pdfMapper.updateById(pdf);
        writeVersionLog(order, user, "VOID_PDF", request.voidReason, pdf.getVersion());
        writeLog(user, servletRequest, "VOID_ACCEPTANCE_PDF", "WORK_ORDER_PDF", pdf.getId(), order);
        return toPdfVO(pdf);
    }

    @Override
    public List<AcceptanceLockHistoryVO> lockHistory(Long workOrderId) {
        WorkOrder order = requireAdminWorkOrder(workOrderId, CurrentUserContext.require());
        List<AcceptanceLockHistoryVO> history = new java.util.ArrayList<>();
        operationLogMapper.selectAll().stream()
                .filter(item -> order.getProjectId().equals(item.getProjectId()))
                .filter(item -> item.getOperationType() != null && (item.getOperationType().contains("LOCK_ACCEPTANCE")
                        || item.getOperationType().contains("UNLOCK_ACCEPTANCE")
                        || item.getOperationType().contains("RELOCK_ACCEPTANCE")
                        || item.getOperationType().contains("VOID_ACCEPTANCE_PDF")))
                .forEach(item -> {
                    AcceptanceLockHistoryVO vo = new AcceptanceLockHistoryVO();
                    vo.sourceType = "OPERATION_LOG";
                    vo.sourceId = item.getId();
                    vo.operationType = item.getOperationType();
                    vo.description = item.getBusinessNo();
                    vo.operatorId = item.getOperatorId();
                    vo.operatorName = item.getOperatorName();
                    vo.operationTime = item.getOperationTime();
                    history.add(vo);
                });
        versionLogMapper.selectAll().stream()
                .filter(item -> workOrderId.equals(item.getWorkOrderId()))
                .filter(item -> item.getChangeType() != null && (item.getChangeType().contains("LOCK_ACCEPTANCE")
                        || item.getChangeType().contains("UNLOCK_ACCEPTANCE")
                        || item.getChangeType().contains("RELOCK_ACCEPTANCE")
                        || item.getChangeType().contains("VOID_PDF")))
                .forEach(item -> {
                    AcceptanceLockHistoryVO vo = new AcceptanceLockHistoryVO();
                    vo.sourceType = "VERSION_LOG";
                    vo.sourceId = item.getId();
                    vo.operationType = item.getChangeType();
                    vo.description = item.getRemark();
                    vo.operatorId = item.getOperatorId();
                    vo.operationTime = item.getCreatedAt();
                    history.add(vo);
                });
        return history.stream().sorted(java.util.Comparator.comparing(item -> item.operationTime, java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder()))).toList();
    }

    private SignatureVO createSignature(WorkOrder order, SignatureRequest request, CurrentUser user, HttpServletRequest servletRequest) {
        if (fileStorageMapper.selectByFileId(request.fileId) == null) {
            throw new BusinessException(ErrorCode.FILE_ERROR, "Signature file does not exist");
        }
        ensureAcceptanceEditable(request.acceptanceId);
        WorkOrderSignature existing = findSignatureByLocalId(order.getId(), request.localId);
        if (existing != null) {
            return toSignatureVO(existing);
        }
        WorkOrderSignature signature = new WorkOrderSignature();
        signature.setSignatureNo("SIG-" + nowNo());
        signature.setWorkOrderId(order.getId());
        signature.setAcceptanceId(request.acceptanceId);
        signature.setFileId(request.fileId);
        signature.setSignatureRole(defaultText(defaultText(request.signatureType, request.signatureRole), "CONSTRUCTION"));
        signature.setSignerUserId(request.signerUserId == null ? user.getUserId() : request.signerUserId);
        signature.setSignerName(defaultText(request.signerName, user.getRealName()));
        signature.setSignerPhone(request.signerPhone);
        signature.setSignedAt(request.signedAt == null ? LocalDateTime.now() : request.signedAt);
        signature.setSignLocation(request.signLocation);
        signature.setLatitude(request.latitude);
        signature.setLongitude(request.longitude);
        signature.setSignatureHash(request.signatureHash);
        signature.setSignatureStatus("SIGNED");
        signature.setUploadStatus("UPLOADED");
        signature.setRetryCount(0);
        fillSync(signature, user, request.localId, request.deviceId);
        signature.setRemark(request.remark);
        signatureMapper.insert(signature);
        signature.setServerId(signature.getId());
        signatureMapper.updateById(signature);
        updateSignatureCount(request.acceptanceId);
        writeLog(user, servletRequest, "CREATE_SIGNATURE", "WORK_ORDER_SIGNATURE", signature.getId(), order);
        return toSignatureVO(signature);
    }

    private AcceptanceVO saveAcceptance(WorkOrder order, WorkOrderAcceptance acceptance, AcceptanceRequest request,
            CurrentUser user, HttpServletRequest servletRequest, String operationType) {
        if ("REJECTED".equals(request.acceptanceStatus) && !StringUtils.hasText(request.rejectReason)) {
            throw new BusinessException(ErrorCode.PDF_ERROR, "Reject reason is required");
        }
        boolean create = acceptance.getId() == null;
        fillAcceptance(acceptance, order, user);
        acceptance.setAcceptanceStatus(defaultText(request.acceptanceStatus, "PENDING"));
        acceptance.setAcceptanceResult(request.acceptanceResult);
        acceptance.setAcceptanceOpinion(request.acceptanceOpinion);
        acceptance.setProblemDesc("REJECTED".equals(request.acceptanceStatus) ? request.rejectReason : request.problemDesc);
        acceptance.setRectificationRequired(defaultInt(request.rectificationRequired, 0));
        acceptance.setRecordSummary(request.recordSummary);
        acceptance.setAttachmentSummary(request.attachmentSummary);
        acceptance.setAcceptanceTime(request.acceptanceTime == null ? LocalDateTime.now() : request.acceptanceTime);
        acceptance.setLocalId(request.localId);
        acceptance.setDeviceId(request.deviceId);
        acceptance.setRemark(request.remark);
        if (create) {
            acceptance.setAcceptanceNo("ACC-" + nowNo());
            acceptance.setPdfGeneratedFlag(0);
            acceptance.setLockedFlag(0);
            acceptance.setSignatureCount(0);
            fillSync(acceptance, user, request.localId, request.deviceId);
            acceptanceMapper.insert(acceptance);
            acceptance.setServerId(acceptance.getId());
        } else {
            acceptance.setVersion(defaultInt(acceptance.getVersion(), 1) + 1);
            acceptance.setUpdatedAt(LocalDateTime.now());
            acceptance.setUpdatedBy(user.getUserId());
        }
        acceptanceMapper.updateById(acceptance);
        applyAcceptanceStatus(order, acceptance.getAcceptanceStatus(), user, request.deviceId);
        writeLog(user, servletRequest, operationType, "WORK_ORDER_ACCEPTANCE", acceptance.getId(), order);
        return toAcceptanceVO(acceptance);
    }

    private void applyAcceptanceStatus(WorkOrder order, String acceptanceStatus, CurrentUser user, String deviceId) {
        String target = null;
        if ("PASSED".equals(acceptanceStatus)) {
            target = "COMPLETED";
        } else if ("REJECTED".equals(acceptanceStatus)) {
            target = "REJECTED";
        }
        if (target == null || target.equals(order.getStatus())) {
            return;
        }
        String from = order.getStatus();
        order.setStatus(target);
        order.setVersion(defaultInt(order.getVersion(), 1) + 1);
        order.setUpdatedAt(LocalDateTime.now());
        order.setUpdatedBy(user.getUserId());
        workOrderMapper.updateById(order);
        writeStatusLog(order.getId(), from, target, "ACCEPTANCE_REVIEW", user, deviceId);
    }

    private void writeStatusLog(Long workOrderId, String fromStatus, String toStatus, String operationType, CurrentUser user, String deviceId) {
        WorkOrderStatusLog log = new WorkOrderStatusLog();
        log.setWorkOrderId(workOrderId);
        log.setFromStatus(fromStatus);
        log.setToStatus(toStatus);
        log.setOperationType(operationType);
        log.setOperationDesc("Acceptance status changed work order status");
        log.setOperatorId(user.getUserId());
        log.setOperationTime(LocalDateTime.now());
        log.setVersion(1);
        log.setSyncStatus("SYNCED");
        log.setDeviceId(deviceId);
        log.setCreatedAt(LocalDateTime.now());
        log.setUpdatedAt(LocalDateTime.now());
        log.setDeletedFlag(0);
        log.setCreatedBy(user.getUserId());
        log.setUpdatedBy(user.getUserId());
        statusLogMapper.insert(log);
        log.setServerId(log.getId());
        statusLogMapper.updateById(log);
    }

    private WorkOrderPdf buildPdf(WorkOrder order, WorkOrderAcceptance acceptance, CurrentUser user, String fileId,
            String snapshot, String localId, String deviceId) {
        WorkOrderPdf pdf = new WorkOrderPdf();
        pdf.setPdfNo("PDF-" + nowNo());
        pdf.setWorkOrderId(order.getId());
        pdf.setAcceptanceId(acceptance == null ? null : acceptance.getId());
        pdf.setFileId(fileId);
        pdf.setWorkOrderNo(order.getWorkOrderNo());
        pdf.setProjectName(acceptance == null ? projectName(order) : acceptance.getProjectName());
        pdf.setConstructionUserName(acceptance == null ? null : acceptance.getConstructionUserName());
        pdf.setAcceptanceUserName(acceptance == null ? user.getRealName() : acceptance.getAcceptanceUserName());
        pdf.setAcceptanceTime(acceptance == null ? LocalDateTime.now() : acceptance.getAcceptanceTime());
        pdf.setSignatureFileIds(String.join(",", signatureMapper.selectByWorkOrderId(order.getId()).stream().map(WorkOrderSignature::getFileId).toList()));
        pdf.setRecordSummary(acceptance == null ? null : acceptance.getRecordSummary());
        pdf.setPdfContentSnapshot(snapshot);
        pdf.setPdfStatus("GENERATED");
        pdf.setGeneratedBy(user.getUserId());
        pdf.setGeneratedAt(LocalDateTime.now());
        pdf.setLockedFlag(1);
        pdf.setArchiveStatus("NONE");
        pdf.setPreviewEnabled(1);
        pdf.setDownloadEnabled(1);
        pdf.setUploadStatus("UPLOADED");
        pdf.setRetryCount(0);
        fillSync(pdf, user, localId, deviceId);
        return pdf;
    }

    private void fillAcceptance(WorkOrderAcceptance acceptance, WorkOrder order, CurrentUser user) {
        acceptance.setWorkOrderId(order.getId());
        acceptance.setProjectId(order.getProjectId());
        acceptance.setWorkOrderNo(order.getWorkOrderNo());
        acceptance.setProjectName(projectName(order));
        acceptance.setConstructionUserId(user.getUserId());
        acceptance.setConstructionUserName(user.getRealName());
        if (acceptance.getAcceptanceUserId() == null) {
            acceptance.setAcceptanceUserId(user.getUserId());
            acceptance.setAcceptanceUserName(user.getRealName());
        }
    }

    private String projectName(WorkOrder order) {
        ProjectInfo project = projectInfoMapper.selectById(order.getProjectId());
        return project == null ? null : project.getProjectName();
    }

    private WorkOrderAcceptance latestAcceptance(Long workOrderId) {
        return acceptanceMapper.selectByWorkOrderId(workOrderId).stream().findFirst().orElse(null);
    }

    private WorkOrderAcceptance requireAcceptance(Long workOrderId, Long acceptanceId) {
        WorkOrderAcceptance acceptance = acceptanceMapper.selectById(acceptanceId);
        if (acceptance == null || !workOrderId.equals(acceptance.getWorkOrderId())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Acceptance not found");
        }
        return acceptance;
    }

    private void ensureAcceptanceEditable(Long acceptanceId) {
        if (acceptanceId == null) {
            return;
        }
        WorkOrderAcceptance acceptance = acceptanceMapper.selectById(acceptanceId);
        if (acceptance != null && Integer.valueOf(1).equals(acceptance.getLockedFlag())) {
            throw new BusinessException(ErrorCode.SYNC_ERROR, "Acceptance is locked");
        }
    }

    private void lockAcceptance(WorkOrderAcceptance acceptance, CurrentUser user, String reason) {
        if (acceptance == null) {
            return;
        }
        acceptance.setPdfGeneratedFlag(1);
        acceptance.setLockedFlag(1);
        acceptance.setLockedAt(LocalDateTime.now());
        acceptance.setLockedBy(user.getUserId());
        acceptance.setLockReason(reason);
        acceptance.setVersion(defaultInt(acceptance.getVersion(), 1) + 1);
        acceptance.setUpdatedAt(LocalDateTime.now());
        acceptance.setUpdatedBy(user.getUserId());
        acceptanceMapper.updateById(acceptance);
    }

    private void updateSignatureCount(Long acceptanceId) {
        if (acceptanceId == null) {
            return;
        }
        WorkOrderAcceptance acceptance = acceptanceMapper.selectById(acceptanceId);
        if (acceptance != null) {
            acceptance.setSignatureCount((int) signatureMapper.selectAll().stream().filter(s -> acceptanceId.equals(s.getAcceptanceId())).count());
            acceptance.setUpdatedAt(LocalDateTime.now());
            acceptanceMapper.updateById(acceptance);
        }
    }

    private WorkOrder requireMobileWorkOrder(Long workOrderId, CurrentUser user) {
        WorkOrder order = requireOrder(workOrderId);
        if (!canAccessWorkOrder(user, order)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "No permission for work order");
        }
        return order;
    }

    private WorkOrder requireAdminWorkOrder(Long workOrderId, CurrentUser user) {
        WorkOrder order = requireOrder(workOrderId);
        if (!dataScopeService.canAccessAll(user) && !dataScopeService.canAccessProject(user, order.getProjectId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "No permission for work order");
        }
        return order;
    }

    private WorkOrder requireOrder(Long id) {
        WorkOrder order = workOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Work order not found");
        }
        return order;
    }

    private boolean canAccessWorkOrder(CurrentUser user, WorkOrder order) {
        return dataScopeService.canAccessAll(user) || dataScopeService.canAccessProject(user, order.getProjectId())
                || user.getUserId().equals(order.getMaintainerId()) || user.getUserId().equals(order.getLeaderId())
                || assignmentMapper.selectByWorkOrderId(order.getId()).stream().map(WorkOrderAssignment::getAssigneeId).anyMatch(user.getUserId()::equals);
    }

    private WorkOrderSignature findSignatureByLocalId(Long workOrderId, String localId) {
        if (!StringUtils.hasText(localId)) {
            return null;
        }
        return signatureMapper.selectByWorkOrderId(workOrderId).stream().filter(s -> localId.equals(s.getLocalId())).findFirst().orElse(null);
    }

    private WorkOrderAcceptance findAcceptanceByLocalId(Long workOrderId, String localId) {
        if (!StringUtils.hasText(localId)) {
            return null;
        }
        return acceptanceMapper.selectByWorkOrderId(workOrderId).stream().filter(a -> localId.equals(a.getLocalId())).findFirst().orElse(null);
    }

    private WorkOrderPdf findPdfByLocalId(Long workOrderId, String localId) {
        if (!StringUtils.hasText(localId)) {
            return null;
        }
        return pdfMapper.selectByWorkOrderId(workOrderId).stream().filter(p -> localId.equals(p.getLocalId())).findFirst().orElse(null);
    }

    private void fillSync(Object entity, CurrentUser user, String localId, String deviceId) {
        LocalDateTime now = LocalDateTime.now();
        if (entity instanceof WorkOrderAcceptance e) { e.setLocalId(localId); e.setVersion(1); e.setSyncStatus("SYNCED"); e.setDeviceId(deviceId); e.setOperatorId(user.getUserId()); e.setConflictFlag(0); e.setCreatedAt(now); e.setUpdatedAt(now); e.setDeletedFlag(0); e.setCreatedBy(user.getUserId()); e.setUpdatedBy(user.getUserId()); }
        if (entity instanceof WorkOrderSignature e) { e.setLocalId(localId); e.setVersion(1); e.setSyncStatus("SYNCED"); e.setDeviceId(deviceId); e.setOperatorId(user.getUserId()); e.setConflictFlag(0); e.setCreatedAt(now); e.setUpdatedAt(now); e.setDeletedFlag(0); e.setCreatedBy(user.getUserId()); e.setUpdatedBy(user.getUserId()); }
        if (entity instanceof WorkOrderPdf e) { e.setLocalId(localId); e.setVersion(1); e.setSyncStatus("SYNCED"); e.setDeviceId(deviceId); e.setOperatorId(user.getUserId()); e.setConflictFlag(0); e.setCreatedAt(now); e.setUpdatedAt(now); e.setDeletedFlag(0); e.setCreatedBy(user.getUserId()); e.setUpdatedBy(user.getUserId()); }
    }

    private SignatureVO toSignatureVO(WorkOrderSignature s) {
        SignatureVO vo = new SignatureVO(); vo.id=s.getId(); vo.serverId=s.getServerId(); vo.localId=s.getLocalId(); vo.workOrderId=s.getWorkOrderId(); vo.acceptanceId=s.getAcceptanceId(); vo.fileId=s.getFileId(); vo.signatureRole=s.getSignatureRole(); vo.signerUserId=s.getSignerUserId(); vo.signerName=s.getSignerName(); vo.signedAt=s.getSignedAt(); vo.signatureStatus=s.getSignatureStatus(); vo.version=s.getVersion(); vo.updatedAt=s.getUpdatedAt(); vo.syncStatus=s.getSyncStatus(); vo.previewUrl="/api/files/"+s.getFileId()+"/preview"; vo.downloadUrl="/api/files/"+s.getFileId()+"/download"; return vo;
    }

    private AcceptanceVO toAcceptanceVO(WorkOrderAcceptance a) {
        AcceptanceVO vo = new AcceptanceVO(); vo.id=a.getId(); vo.serverId=a.getServerId(); vo.localId=a.getLocalId(); vo.acceptanceNo=a.getAcceptanceNo(); vo.workOrderId=a.getWorkOrderId(); vo.projectId=a.getProjectId(); vo.workOrderNo=a.getWorkOrderNo(); vo.projectName=a.getProjectName(); vo.acceptanceStatus=a.getAcceptanceStatus(); vo.acceptanceResult=a.getAcceptanceResult(); vo.acceptanceOpinion=a.getAcceptanceOpinion(); vo.problemDesc=a.getProblemDesc(); vo.rectificationRequired=a.getRectificationRequired(); vo.pdfGeneratedFlag=a.getPdfGeneratedFlag(); vo.lockedFlag=a.getLockedFlag(); vo.signatureCount=a.getSignatureCount(); vo.version=a.getVersion(); vo.updatedAt=a.getUpdatedAt(); vo.syncStatus=a.getSyncStatus(); return vo;
    }

    private PdfVO toPdfVO(WorkOrderPdf p) {
        PdfVO vo = new PdfVO(); vo.id=p.getId(); vo.serverId=p.getServerId(); vo.localId=p.getLocalId(); vo.pdfNo=p.getPdfNo(); vo.workOrderId=p.getWorkOrderId(); vo.acceptanceId=p.getAcceptanceId(); vo.fileId=p.getFileId(); vo.pdfStatus=p.getPdfStatus(); vo.lockedFlag=p.getLockedFlag(); vo.generatedAt=p.getGeneratedAt(); vo.previewUrl="/api/files/"+p.getFileId()+"/preview"; vo.downloadUrl="/api/files/"+p.getFileId()+"/download"; vo.version=p.getVersion(); vo.updatedAt=p.getUpdatedAt(); vo.syncStatus=p.getSyncStatus(); return vo;
    }

    private AcceptanceLockStatusVO toLockStatus(Long workOrderId) {
        WorkOrderAcceptance acceptance = latestAcceptance(workOrderId);
        WorkOrderPdf pdf = pdfMapper.selectByWorkOrderId(workOrderId).stream().findFirst().orElse(null);
        AcceptanceLockStatusVO vo = new AcceptanceLockStatusVO();
        vo.workOrderId = workOrderId;
        vo.acceptanceId = acceptance == null ? null : acceptance.getId();
        vo.pdfId = pdf == null ? null : pdf.getId();
        vo.lockedFlag = acceptance == null ? 0 : defaultInt(acceptance.getLockedFlag(), 0);
        vo.lockedBy = acceptance == null ? null : acceptance.getLockedBy();
        vo.lockedAt = acceptance == null ? null : acceptance.getLockedAt();
        vo.lockReason = acceptance == null ? null : acceptance.getLockReason();
        return vo;
    }

    private byte[] simplePdf(WorkOrder order, WorkOrderAcceptance acceptance) {
        String text = "Offshore Acceptance\\nWorkOrder: " + order.getWorkOrderNo() + "\\nProject: " + projectName(order)
                + "\\nLocation: " + order.getWorkLocation() + "\\nOpinion: " + defaultText(acceptance.getAcceptanceOpinion(), "")
                + "\\nGeneratedAt: " + LocalDateTime.now();
        String stream = "BT /F1 12 Tf 72 720 Td (" + text.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)") + ") Tj ET";
        String pdf = "%PDF-1.4\n1 0 obj<<>>endobj\n2 0 obj<</Type/Catalog/Pages 3 0 R>>endobj\n3 0 obj<</Type/Pages/Count 1/Kids[4 0 R]>>endobj\n4 0 obj<</Type/Page/Parent 3 0 R/MediaBox[0 0 595 842]/Resources<</Font<</F1 5 0 R>>>>/Contents 6 0 R>>endobj\n5 0 obj<</Type/Font/Subtype/Type1/BaseFont/Helvetica>>endobj\n6 0 obj<</Length " + stream.length() + ">>stream\n" + stream + "\nendstream endobj\ntrailer<</Root 2 0 R>>\n%%EOF";
        return pdf.getBytes(StandardCharsets.US_ASCII);
    }

    private Path resolve(String relativePath) { Path p = storageRoot.resolve(relativePath).normalize(); if (!p.startsWith(storageRoot)) throw new BusinessException(ErrorCode.FILE_ERROR, "Invalid file path"); return p; }
    private String sha256(byte[] bytes) { try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes)); } catch (Exception e) { throw new BusinessException(ErrorCode.SYSTEM_ERROR); } }
    private String nowNo() { return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + UUID.randomUUID().toString().substring(0, 4).toUpperCase(); }
    private String defaultText(String v, String d) { return StringUtils.hasText(v) ? v : d; }
    private Integer defaultInt(Integer v, Integer d) { return v == null ? d : v; }

    private void writeVersionLog(WorkOrder order, CurrentUser user, String changeType, String remark, Integer version) {
        WorkOrderVersionLog log = new WorkOrderVersionLog();
        log.setWorkOrderId(order.getId());
        log.setWorkOrderNo(order.getWorkOrderNo());
        log.setVersion(version);
        log.setPreviousVersion(version == null ? null : version - 1);
        log.setChangeSource("PC");
        log.setChangeType(changeType);
        log.setOperatorId(user.getUserId());
        log.setServerUpdatedAt(LocalDateTime.now());
        log.setCreatedAt(LocalDateTime.now());
        log.setUpdatedAt(LocalDateTime.now());
        log.setDeletedFlag(0);
        log.setCreatedBy(user.getUserId());
        log.setUpdatedBy(user.getUserId());
        log.setRemark(remark);
        versionLogMapper.insert(log);
    }

    private void writeLog(CurrentUser user, HttpServletRequest request, String type, String businessType, Long id, WorkOrder order) {
        OperationLog log = new OperationLog();
        log.setTraceId(TraceIdUtils.currentTraceId());
        log.setOperatorId(user.getUserId());
        log.setOperatorName(user.getRealName());
        log.setRoleCode(String.join(",", user.getRoleCodes()));
        log.setPlatform(request.getRequestURI().contains("/mobile/") ? "MOBILE" : "PC");
        log.setModuleName("ACCEPTANCE");
        log.setOperationType(type);
        log.setBusinessType(businessType);
        log.setBusinessId(String.valueOf(id));
        log.setBusinessNo(order.getWorkOrderNo());
        log.setProjectId(order.getProjectId());
        log.setRequestMethod(request.getMethod());
        log.setRequestPath(request.getRequestURI());
        log.setRequestIp(request.getRemoteAddr());
        log.setUserAgent(request.getHeader("User-Agent"));
        log.setResultStatus("SUCCESS");
        log.setOperationTime(LocalDateTime.now());
        log.setDeletedFlag(0);
        log.setCreatedBy(user.getUserId());
        log.setUpdatedBy(user.getUserId());
        operationLogMapper.insert(log);
    }
}
