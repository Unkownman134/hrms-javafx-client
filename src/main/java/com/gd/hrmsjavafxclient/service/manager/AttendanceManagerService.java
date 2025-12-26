package com.gd.hrmsjavafxclient.service.manager;

import com.gd.hrmsjavafxclient.model.AttendanceRecord;
import com.gd.hrmsjavafxclient.util.ServiceUtil;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 部门经理考勤记录服务 (AttendanceManagerService)
 * 负责获取指定员工的考勤记录。
 */
public class AttendanceManagerService {

    private static final String BASE_ENDPOINT = "/attendance";

    /**
     * 根据员工ID获取考勤记录列表。
     * 🌟 使用 API 文档中的 GET /api/attendance/{EmpID} 接口。
     * @param empId 员工ID
     * @param authToken 认证 Token
     * @return 考勤记录列表 (AttendanceRecord Model)
     * @throws IOException 如果 HTTP 请求失败
     * @throws InterruptedException 如果线程被中断
     */
    public List<AttendanceRecord> getAttendanceRecordsByEmpId(Integer empId, String authToken) throws IOException, InterruptedException {
        if (empId == null) {
            throw new IllegalArgumentException("员工ID不能为空！");
        }

        String endpoint = String.format("%s/%d", BASE_ENDPOINT, empId);

        Optional<List<AttendanceRecord>> result = ServiceUtil.sendGet(
                endpoint,
                authToken,
                new TypeReference<List<AttendanceRecord>>() {}
        );

        return result.orElse(Collections.emptyList());
    }
}