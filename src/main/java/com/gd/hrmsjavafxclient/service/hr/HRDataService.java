package com.gd.hrmsjavafxclient.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.gd.hrmsjavafxclient.model.Department;
import com.gd.hrmsjavafxclient.model.Employee;
import com.gd.hrmsjavafxclient.model.Position;
import com.gd.hrmsjavafxclient.util.ServiceUtil; // 导入通用的 ServiceUtil

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * 人事管理员 (HR) 模块专用的数据服务类。
 * 负责员工、部门、职位等档案信息的 API 通信。
 */
public class HRDataService {

    // 假设 API 端点：
    private static final String EMPLOYEE_ENDPOINT = "/employees";
    private static final String DEPARTMENT_ENDPOINT = "/departments";
    private static final String POSITION_ENDPOINT = "/positions";

    /**
     * 获取所有员工的列表。
     * @param authToken 认证 Token
     * @return 员工列表，失败返回空列表
     */
    public List<Employee> getAllEmployees(String authToken) {
        System.out.println("HRService: 正在获取所有员工数据...");
        try {
            return ServiceUtil.sendGet(
                    EMPLOYEE_ENDPOINT,
                    authToken,
                    new TypeReference<List<Employee>>() {}
            ).orElse(Collections.emptyList());
        } catch (IOException | InterruptedException e) {
            System.err.println("API调用失败：无法获取员工列表。");
            e.printStackTrace();
            return Collections.emptyList();
        } catch (RuntimeException e) {
            System.err.println("API请求异常: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 获取所有部门的列表。
     * @param authToken 认证 Token
     * @return 部门列表，失败返回空列表
     */
    public List<Department> getAllDepartments(String authToken) {
        System.out.println("HRService: 正在获取所有部门数据...");
        try {
            return ServiceUtil.sendGet(
                    DEPARTMENT_ENDPOINT,
                    authToken,
                    new TypeReference<List<Department>>() {}
            ).orElse(Collections.emptyList());
        } catch (IOException | InterruptedException e) {
            System.err.println("API调用失败：无法获取部门列表。");
            e.printStackTrace();
            return Collections.emptyList();
        } catch (RuntimeException e) {
            System.err.println("API请求异常: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 获取所有职位的列表。
     * @param authToken 认证 Token
     * @return 职位列表，失败返回空列表
     */
    public List<Position> getAllPositions(String authToken) {
        System.out.println("HRService: 正在获取所有职位数据...");
        try {
            return ServiceUtil.sendGet(
                    POSITION_ENDPOINT,
                    authToken,
                    new TypeReference<List<Position>>() {}
            ).orElse(Collections.emptyList());
        } catch (IOException | InterruptedException e) {
            System.err.println("API调用失败：无法获取职位列表。");
            e.printStackTrace();
            return Collections.emptyList();
        } catch (RuntimeException e) {
            System.err.println("API请求异常: " + e.getMessage());
            return Collections.emptyList();
        }
    }
}