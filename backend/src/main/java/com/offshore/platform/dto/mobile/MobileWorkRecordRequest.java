package com.offshore.platform.dto.mobile;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MobileWorkRecordRequest {
    private String localId;
    private Integer version;

    @NotBlank(message = "记录类型不能为空")
    private String recordType;

    private LocalDateTime constructionTime;
    private String constructionDesc;
    private String siteCondition;
    private Integer abnormalFlag;
    private String abnormalDesc;
    private String weather;
    private BigDecimal temperature;
    private BigDecimal humidity;
    private String locationName;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private BigDecimal altitude;
    private String deviceId;
    private String remark;

    public String getLocalId() { return localId; }
    public void setLocalId(String localId) { this.localId = localId; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
    public String getRecordType() { return recordType; }
    public void setRecordType(String recordType) { this.recordType = recordType; }
    public LocalDateTime getConstructionTime() { return constructionTime; }
    public void setConstructionTime(LocalDateTime constructionTime) { this.constructionTime = constructionTime; }
    public String getConstructionDesc() { return constructionDesc; }
    public void setConstructionDesc(String constructionDesc) { this.constructionDesc = constructionDesc; }
    public String getSiteCondition() { return siteCondition; }
    public void setSiteCondition(String siteCondition) { this.siteCondition = siteCondition; }
    public Integer getAbnormalFlag() { return abnormalFlag; }
    public void setAbnormalFlag(Integer abnormalFlag) { this.abnormalFlag = abnormalFlag; }
    public String getAbnormalDesc() { return abnormalDesc; }
    public void setAbnormalDesc(String abnormalDesc) { this.abnormalDesc = abnormalDesc; }
    public String getWeather() { return weather; }
    public void setWeather(String weather) { this.weather = weather; }
    public BigDecimal getTemperature() { return temperature; }
    public void setTemperature(BigDecimal temperature) { this.temperature = temperature; }
    public BigDecimal getHumidity() { return humidity; }
    public void setHumidity(BigDecimal humidity) { this.humidity = humidity; }
    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }
    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }
    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }
    public BigDecimal getAltitude() { return altitude; }
    public void setAltitude(BigDecimal altitude) { this.altitude = altitude; }
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
