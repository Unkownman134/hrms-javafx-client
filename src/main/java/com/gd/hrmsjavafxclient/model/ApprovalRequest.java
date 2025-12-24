package com.gd.hrmsjavafxclient.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 审批申请模型 - 严格对应数据库 t_approval_request
 * 修正：忽略后端返回但前端暂未定义的冗余字段
 */
@JsonIgnoreProperties(ignoreUnknown = true) // 🌟 关键：加上这个，不认识的字段就不会报错啦！
public class ApprovalRequest {

    @JsonProperty("requestId")
    private Integer requestId;

    @JsonProperty("requestType")
    private String requestType;

    @JsonProperty("startDate")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonProperty("endDate")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @JsonProperty("reason")
    private String reason;

    @JsonProperty("status")
    private String status;

    @JsonProperty("submitTime")
    // 之前你报错是因为 'T' 的解析问题，这里建议用这个格式
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime submitTime;

    @JsonProperty("empId")
    private Integer empId;

    // 🌟 后端返回了这些，我们也定义一下，防止解析混乱
    @JsonProperty("configId")
    private Integer configId;

    @JsonProperty("currentApproverId")
    private Integer currentApproverId;

    public ApprovalRequest() {}

    // --- Getters and Setters (禁止省略任何代码) ---

    public Integer getRequestId() { return requestId; }
    public void setRequestId(Integer requestId) { this.requestId = requestId; }

    public String getRequestType() { return requestType; }
    public void setRequestType(String requestType) { this.requestType = requestType; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getSubmitTime() { return submitTime; }
    public void setSubmitTime(LocalDateTime submitTime) { this.submitTime = submitTime; }

    public Integer getEmpId() { return empId; }
    public void setEmpId(Integer empId) { this.empId = empId; }

    public Integer getConfigId() { return configId; }
    public void setConfigId(Integer configId) { this.configId = configId; }

    public Integer getCurrentApproverId() { return currentApproverId; }
    public void setCurrentApproverId(Integer currentApproverId) { this.currentApproverId = currentApproverId; }
}