package com.gd.hrmsjavafxclient.service;

import com.gd.hrmsjavafxclient.model.AttendanceRecord;
import com.gd.hrmsjavafxclient.model.SalaryRecord;
import com.gd.hrmsjavafxclient.model.ApprovalRequest;

import java.util.List;
// 不使用 CompletableFuture，改为同步方法

/**
 * 员工自助服务相关的API接口定义 (R5, R6, R7)
 */
public interface EmployeeService {

    /**
     * 查询指定员工的考勤记录
     * API: GET /api/attendance/{EmpID}
     * @param empId 员工ID
     * @param yearMonth 格式如 "2025-11"
     * @param authToken 认证Token
     * @return 考勤记录列表
     */
    List<AttendanceRecord> getAttendanceRecords(int empId, String yearMonth, String authToken) throws Exception;

    /**
     * 查询指定员工的工资条记录
     * API: GET /api/salary/history/{empId}
     * @param empId 员工ID
     * @param year 年份，如 2025
     * @param authToken 认证Token
     * @return 工资条记录列表
     */
    List<SalaryRecord> getSalaryRecords(int empId, int year, String authToken) throws Exception;

    /**
     * 提交新的申请 (请假/报销等)
     * API: POST /api/approval-requests
     * @param request 审批请求 Model (包含 applicantId = EmpID)
     * @param authToken 认证Token
     * @return 提交是否成功
     */
    boolean submitApplication(ApprovalRequest request, String authToken) throws Exception;
}