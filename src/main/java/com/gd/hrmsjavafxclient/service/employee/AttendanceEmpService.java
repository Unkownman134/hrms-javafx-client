package com.gd.hrmsjavafxclient.service.employee;

import com.fasterxml.jackson.core.type.TypeReference;
import com.gd.hrmsjavafxclient.model.AttendanceRecord;
import com.gd.hrmsjavafxclient.util.ServiceUtil;
import java.util.Collections; // 🌟 新增：导入 Collections
import java.util.List;

/**
 * 员工考勤记录查询服务 (R5)
 * 修正：适配 ServiceUtil.sendGet() 方法签名和返回值处理。
 */
public class AttendanceEmpService {

    /**
     * 获取员工的考勤记录列表。
     * @param empId 员工 ID
     * @param yearMonth 忽略此参数 (后端 API 未实现)
     * @param authToken 认证 Token
     * @return 考勤记录列表，失败返回空列表
     */
    public List<AttendanceRecord> getAttendanceRecords(int empId, String yearMonth, String authToken) { // 🌟 移除 throws Exception
        // API: GET /api/attendance/{EmpID}
        String path = String.format("/attendance/%d", empId);

        System.out.println("AttendanceEmpService: 正在获取员工考勤记录...");
        try {
            // 🌟 修正：使用 ServiceUtil.sendGet 并处理 Optional，失败返回空列表
            return ServiceUtil.sendGet(
                    path,
                    authToken,
                    new TypeReference<List<AttendanceRecord>>() {}
            ).orElse(Collections.emptyList());
        } catch (Exception e) { // 捕获所有异常
            System.err.println("API调用失败：无法获取员工考勤记录。");
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}