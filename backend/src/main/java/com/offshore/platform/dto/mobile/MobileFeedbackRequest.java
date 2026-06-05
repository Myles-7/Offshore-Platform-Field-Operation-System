package com.offshore.platform.dto.mobile;

import jakarta.validation.constraints.Size;

public class MobileFeedbackRequest {
    @Size(max = 500, message = "反馈内容不能超过500字")
    private String feedback;

    private Integer abnormalFlag;

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public Integer getAbnormalFlag() {
        return abnormalFlag;
    }

    public void setAbnormalFlag(Integer abnormalFlag) {
        this.abnormalFlag = abnormalFlag;
    }
}
