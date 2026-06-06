package com.offshore.platform.vo.sync;

public class ConflictFieldCompareVO {
    public String fieldName;
    public String fieldLabel;
    public String clientValue;
    public String serverValue;
    public boolean conflict;
    public String suggestedValue;
}
