package com.offshore.platform.vo.ai;

import java.util.ArrayList;
import java.util.List;

public class AiResultDetailVO extends AiResultVO {
    public Object workOrder;
    public Object workRecord;
    public Object attachment;
    public Object resultImageAttachment;
    public List<AiReviewRecordVO> reviewRecords = new ArrayList<>();
}
