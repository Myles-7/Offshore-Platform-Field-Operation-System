package com.offshore.platform.service;

import com.offshore.platform.dto.acceptance.AcceptanceRequest;
import com.offshore.platform.dto.acceptance.AcceptanceLockRequest;
import com.offshore.platform.dto.acceptance.AcceptanceRelockRequest;
import com.offshore.platform.dto.acceptance.AcceptanceReviewRequest;
import com.offshore.platform.dto.acceptance.AcceptanceUnlockRequest;
import com.offshore.platform.dto.acceptance.PdfGenerateRequest;
import com.offshore.platform.dto.acceptance.PdfMetadataRequest;
import com.offshore.platform.dto.acceptance.PdfVoidRequest;
import com.offshore.platform.dto.acceptance.SignatureRequest;
import com.offshore.platform.vo.acceptance.AcceptanceVO;
import com.offshore.platform.vo.acceptance.AcceptanceLockHistoryVO;
import com.offshore.platform.vo.acceptance.AcceptanceLockStatusVO;
import com.offshore.platform.vo.acceptance.PdfVO;
import com.offshore.platform.vo.acceptance.SignatureVO;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

public interface AcceptanceService {
    SignatureVO createSignature(Long workOrderId, SignatureRequest request, HttpServletRequest servletRequest);
    List<SignatureVO> listMobileSignatures(Long workOrderId);
    AcceptanceVO submitAcceptance(Long workOrderId, AcceptanceRequest request, HttpServletRequest servletRequest);
    AcceptanceVO updateAcceptance(Long workOrderId, Long acceptanceId, AcceptanceRequest request, HttpServletRequest servletRequest);
    PdfVO savePdfMetadata(Long workOrderId, PdfMetadataRequest request, HttpServletRequest servletRequest);
    List<PdfVO> listMobilePdfs(Long workOrderId);
    List<SignatureVO> listSignatures(Long workOrderId);
    SignatureVO createAdminSignature(Long workOrderId, SignatureRequest request, HttpServletRequest servletRequest);
    List<AcceptanceVO> listAcceptance(Long workOrderId);
    AcceptanceVO reviewAcceptance(Long workOrderId, AcceptanceReviewRequest request, HttpServletRequest servletRequest);
    PdfVO generatePdf(Long workOrderId, HttpServletRequest servletRequest);
    PdfVO generatePdf(Long workOrderId, PdfGenerateRequest request, HttpServletRequest servletRequest);
    List<PdfVO> listPdfs(Long workOrderId);
    PdfVO latestPdf(Long workOrderId);
    PdfVO archivePdf(Long workOrderId, HttpServletRequest servletRequest);
    AcceptanceLockStatusVO lockStatus(Long workOrderId);
    AcceptanceLockStatusVO lockAcceptance(Long workOrderId, AcceptanceLockRequest request, HttpServletRequest servletRequest);
    AcceptanceLockStatusVO unlockAcceptance(Long workOrderId, AcceptanceUnlockRequest request, HttpServletRequest servletRequest);
    AcceptanceLockStatusVO relockAcceptance(Long workOrderId, AcceptanceRelockRequest request, HttpServletRequest servletRequest);
    PdfVO voidPdf(Long workOrderId, Long pdfId, PdfVoidRequest request, HttpServletRequest servletRequest);
    List<AcceptanceLockHistoryVO> lockHistory(Long workOrderId);
}
