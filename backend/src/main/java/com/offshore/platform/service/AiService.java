package com.offshore.platform.service;

import com.offshore.platform.dto.ai.AiModelRequest;
import com.offshore.platform.dto.ai.AiBatchReviewRequest;
import com.offshore.platform.dto.ai.AiResultRequest;
import com.offshore.platform.dto.ai.AiResultQueryRequest;
import com.offshore.platform.dto.ai.AiReviewRequest;
import com.offshore.platform.dto.ai.AiDefectBoxRequest;
import com.offshore.platform.vo.ai.AiModelVO;
import com.offshore.platform.vo.ai.AiDefectBoxVO;
import com.offshore.platform.vo.ai.AiResultDetailVO;
import com.offshore.platform.vo.ai.AiResultVO;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

public interface AiService {
    List<AiModelVO> listModels();

    AiModelVO createModel(AiModelRequest request);

    AiModelVO activateModel(Long id);

    AiResultVO createResult(AiResultRequest request);

    AiResultVO getResult(Long id);
    AiResultDetailVO getResultDetail(Long id);

    List<AiResultVO> adminResults(AiResultQueryRequest request);

    List<AiResultVO> adminWorkOrderResults(Long workOrderId);

    List<AiResultVO> adminRecordResults(Long recordId);

    List<AiResultVO> mobileWorkOrderResults(Long workOrderId);

    AiResultVO review(Long id, AiReviewRequest request, HttpServletRequest servletRequest);

    AiDefectBoxVO reviewBox(Long id, Long boxId, AiDefectBoxRequest request, HttpServletRequest servletRequest);

    List<AiResultVO> batchReview(AiBatchReviewRequest request, HttpServletRequest servletRequest);
}
