package com.gd.hrmsjavafxclient.service.employee;

import com.fasterxml.jackson.core.type.TypeReference;
import com.gd.hrmsjavafxclient.model.SalaryRecord;
import com.gd.hrmsjavafxclient.util.ServiceUtil;
import java.util.Collections;
import java.util.List;

/**
 * 员工工资条查询服务 (R6)
 * 修正：适配 ServiceUtil.sendGet() 方法签名和返回值处理。
 */
public class SalaryEmpService {

    /**
     * 获取员工的工资条记录列表。
     * @param empId 员工 ID
     * @param year 查询年份
     * @param authToken 认证 Token
     * @return 工资条记录列表，失败返回空列表
     */
    public List<SalaryRecord> getSalaryRecords(int empId, int year, String authToken) {
        String path = String.format("/salary/history/%d?year=%d", empId, year);

        System.out.println("SalaryEmpService: 正在获取员工工资条记录 (年份: " + year + ")...");
        try {
            return ServiceUtil.sendGet(
                    path,
                    authToken,
                    new TypeReference<List<SalaryRecord>>() {}
            ).orElse(Collections.emptyList());
        } catch (Exception e) {
            System.err.println("API调用失败：无法获取员工工资条记录。");
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}