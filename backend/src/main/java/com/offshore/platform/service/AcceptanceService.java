package com.offshore.platform.service;

import com.offshore.platform.dto.acceptance.AcceptanceRequest;
import com.offshore.platform.dto.acceptance.AcceptanceReviewRequest;
import com.offshore.platform.dto.acceptance.PdfMetadataRequest;
import com.offshore.platform.dto.acceptance.SignatureRequest;
import com.offshore.platform.vo.acceptance.AcceptanceVO;
import com.offshore.platform.vo.acceptance.PdfVO;
import com.offshore.platform.vo.acceptance.SignatureVO;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

public interface AcceptanceService {
    SignatureVO createSignature(Long workOrderId, SignatureRequest request, HttpServletRequest servletRequest);
    AcceptanceVO submitAcceptance(Long workOrderId, AcceptanceRequest request, HttpServletRequest servletRequest);
    PdfVO savePdfMetadata(Long workOrderId, PdfMetadataRequest request, HttpServletRequest servletRequest);
    List<SignatureVO> listSignatures(Long workOrderId);
    List<AcceptanceVO> listAcceptance(Long workOrderId);
    AcceptanceVO reviewAcceptance(Long workOrderId, AcceptanceReviewRequest request, HttpServletRequest servletRequest);
    PdfVO generatePdf(Long workOrderId, HttpServletRequest servletRequest);
    List<PdfVO> listPdfs(Long workOrderId);
    PdfVO latestPdf(Long workOrderId);
}
