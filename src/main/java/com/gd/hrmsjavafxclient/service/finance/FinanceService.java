package com.gd.hrmsjavafxclient.service.finance;

import com.fasterxml.jackson.core.type.TypeReference;
import com.gd.hrmsjavafxclient.model.Employee;
import com.gd.hrmsjavafxclient.model.SalaryRecord;
import com.gd.hrmsjavafxclient.util.ServiceUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 💰 财务业务逻辑类
 * 已经抛弃了 SalaryCalculateRequest，直接使用 Map 构造请求体！
 */
public class FinanceService {

    /**
     * 👥 获取所有员工列表
     */
    public List<Employee> getAllEmployees(String token) throws Exception {
        Optional<List<Employee>> result = ServiceUtil.sendGet(
                "/employees",
                token,
                new TypeReference<List<Employee>>() {}
        );
        return result.orElseThrow(() -> new RuntimeException("未能获取员工列表"));
    }

    /**
     * 核心修正：使用 Map 确保 JSON 结构完全符合后端要求
     * 后端期待: { "empId": Integer, "month": "yyyy-MM" }
     */
    public SalaryRecord calculateSalary(String token, Integer empId, String month) throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("empId", empId);
        requestBody.put("month", month);

        Optional<SalaryRecord> result = ServiceUtil.sendRequest(
                "/salary/calculate",
                token,
                requestBody,
                "POST",
                new TypeReference<SalaryRecord>() {}
        );

        return result.orElseThrow(() -> new RuntimeException("工资计算失败，后端未返回有效记录。"));
    }

    /**
     * 📋 获取所有员工的工资记录
     */
    public List<SalaryRecord> getAllSalaryRecords(String token) throws Exception {
        Optional<List<SalaryRecord>> result = ServiceUtil.sendGet(
                "/salary/all",
                token,
                new TypeReference<List<SalaryRecord>>() {}
        );
        return result.orElseThrow(() -> new RuntimeException("未能获取到工资记录列表。"));
    }

    /**
     * 📜 获取指定员工的历史工资记录
     */
    public List<SalaryRecord> getSalaryHistory(String token, Integer empId) throws Exception {
        Optional<List<SalaryRecord>> result = ServiceUtil.sendGet(
                "/salary/history/" + empId,
                token,
                new TypeReference<List<SalaryRecord>>() {}
        );
        return result.orElseThrow(() -> new RuntimeException("未能获取到个人工资历史。"));
    }
}