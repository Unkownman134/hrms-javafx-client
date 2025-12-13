package com.gd.hrmsjavafxclient.model;

import java.time.LocalDate;

/**
 * 审批申请 Model (用于提交和历史查询)
 */
public class ApprovalRequest {
    // 用于提交申请的字段
    private String applicationType; // 申请类型 (例如：请假/报销)
    private LocalDate relatedDate; // 关联日期
    private String relatedDetail; // 关联事项详情 (例如：请假时长/报销金额)
    private String description;
    private int applicantId; // 申请人ID

    // 用于历史查询的字段
    private int requestId;
    private String status; // 状态：待审批/已通过/已拒绝/已撤销
    private LocalDate submissionDate; // 提交日期

    public ApprovalRequest() {}

    // --- Getters and Setters ---
    public String getApplicationType() { return applicationType; }
    public void setApplicationType(String applicationType) { this.applicationType = applicationType; }

    public LocalDate getRelatedDate() { return relatedDate; }
    public void setRelatedDate(LocalDate relatedDate) { this.relatedDate = relatedDate; }

    public String getRelatedDetail() { return relatedDetail; }
    public void setRelatedDetail(String relatedDetail) { this.relatedDetail = relatedDetail; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getApplicantId() { return applicantId; }
    public void setApplicantId(int applicantId) { this.applicantId = applicantId; }

    public int getRequestId() { return requestId; }
    public void setRequestId(int requestId) { this.requestId = requestId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getSubmissionDate() { return submissionDate; }
    public void setSubmissionDate(LocalDate submissionDate) { this.submissionDate = submissionDate; }
}