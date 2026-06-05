package com.offshore.platform.dto.mobile;

import jakarta.validation.constraints.Size;

public class MobileSubmitAcceptanceRequest {
    @Size(max = 500, message = "提交说明不能超过500字")
    private String submitDesc;

    public String getSubmitDesc() {
        return submitDesc;
    }

    public void setSubmitDesc(String submitDesc) {
        this.submitDesc = submitDesc;
    }
}
