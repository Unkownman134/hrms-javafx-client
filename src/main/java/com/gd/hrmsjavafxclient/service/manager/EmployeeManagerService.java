package com.gd.hrmsjavafxclient.service.manager;

import com.gd.hrmsjavafxclient.model.Employee;
import com.gd.hrmsjavafxclient.util.ServiceUtil;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 部门经理员工信息服务 (EmployeeManagerService)
 * 负责获取所有员工列表 (P5)，供 Manager Controller 筛选本部门员工。
 */
public class EmployeeManagerService {

    private static final String ENDPOINT = "/employees";

    /**
     * 获取所有员工信息列表。
     * @param authToken 认证 Token
     * @return 所有员工列表 (Employee Model)
     * @throws IOException 如果 HTTP 请求失败
     */
    public List<Employee> getAllEmployees(String authToken) throws IOException, InterruptedException {

        Optional<List<Employee>> result = ServiceUtil.sendGet(
                ENDPOINT,
                authToken,
                new TypeReference<List<Employee>>() {}
        );

        return result.orElse(Collections.emptyList());
    }
}