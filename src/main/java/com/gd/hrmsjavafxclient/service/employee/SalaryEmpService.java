package com.gd.hrmsjavafxclient.service.employee;

import com.fasterxml.jackson.core.type.TypeReference;
import com.gd.hrmsjavafxclient.model.SalaryRecord;
import com.gd.hrmsjavafxclient.util.ServiceUtil; // 🌟 导入 ServiceUtil
import java.util.List;

/**
 * 员工工资条查询服务 (R6)
 */
public class SalaryEmpService {

    // 假设这是 EmployeeService 接口中 getSalaryRecords 的实现逻辑
    public List<SalaryRecord> getSalaryRecords(int empId, int year, String authToken) throws Exception {
        // API: GET /api/salary/history/{empId}?year=YYYY
        String path = String.format("/salary/history/%d?year=%d", empId, year);

        // 只需要 3 个参数：path, authToken, TypeReference
        return ServiceUtil.sendGetRequestAndParseList(
                path,
                authToken,
                new TypeReference<List<SalaryRecord>>() {}
        );
    }
}