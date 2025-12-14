package com.gd.hrmsjavafxclient.service.employee;

import com.fasterxml.jackson.core.type.TypeReference;
import com.gd.hrmsjavafxclient.model.AttendanceRecord;
import com.gd.hrmsjavafxclient.util.ServiceUtil; // 🌟 导入 ServiceUtil
import java.util.List;

/**
 * 员工考勤记录查询服务 (R5)
 */
public class AttendanceEmpService {

    // 假设这是 EmployeeService 接口中 getAttendanceRecords 的实现逻辑
    public List<AttendanceRecord> getAttendanceRecords(int empId, String yearMonth, String authToken) throws Exception {
        // 🌟 修正：后端API不支持月份筛选，我们只请求该员工的所有考勤记录
        // API: GET /api/attendance/{EmpID}
        String path = String.format("/attendance/%d", empId);

        // 只需要 3 个参数：path, authToken, TypeReference
        return ServiceUtil.sendGetRequestAndParseList(
                path,
                authToken,
                new TypeReference<List<AttendanceRecord>>() {}
        );
    }
}