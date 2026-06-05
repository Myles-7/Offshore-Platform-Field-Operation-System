package com.offshore.platform.dto.mobile;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

public class MobileCheckItemBatchRequest {
    @Valid
    @NotEmpty(message = "检查项不能为空")
    private List<MobileCheckItemRequest> items = new ArrayList<>();

    public List<MobileCheckItemRequest> getItems() {
        return items;
    }

    public void setItems(List<MobileCheckItemRequest> items) {
        this.items = items;
    }
}
