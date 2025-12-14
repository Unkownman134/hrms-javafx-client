package com.gd.hrmsjavafxclient.service.hr;

import com.fasterxml.jackson.core.type.TypeReference;
import com.gd.hrmsjavafxclient.model.Candidate;
import com.gd.hrmsjavafxclient.model.Department;
import com.gd.hrmsjavafxclient.model.Employee;
import com.gd.hrmsjavafxclient.model.Position;
import com.gd.hrmsjavafxclient.util.ServiceUtil;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 人事管理员 (HR) 模块专用的数据服务类。
 * 负责员工、部门、职位、招聘等档案信息的 API 通信。
 */
public class HRDataService {

    // 假设 API 端点：
    private static final String EMPLOYEE_ENDPOINT = "/employees";
    private static final String DEPARTMENT_ENDPOINT = "/departments";
    private static final String POSITION_ENDPOINT = "/positions";
    // 招聘管理 API 端点
    private static final String CANDIDATE_ENDPOINT = "/candidates";

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

    // 🌟 修正/新增方法 1: 获取所有候选人
    /**
     * 获取所有候选人的列表。
     * @param authToken 认证 Token
     * @return 候选人列表，失败返回空列表
     */
    public List<Candidate> getAllCandidates(String authToken) {
        System.out.println("HRService: 正在获取所有候选人数据...");
        try {
            return ServiceUtil.sendGet(
                    CANDIDATE_ENDPOINT,
                    authToken,
                    new TypeReference<List<Candidate>>() {}
            ).orElse(Collections.emptyList());
        } catch (Exception e) { // 统一捕获异常
            System.err.println("API调用失败：无法获取候选人列表。");
            e.printStackTrace();
            return Collections.emptyList();
        }
    }


    // 🌟 修正/新增方法 2: 新增候选人
    /**
     * 新增候选人 (POST /api/candidates)。
     * 关键修正：API 调用失败时抛出异常，以便 RecruitmentController 捕获并显示详细错误。
     * @param candidate 候选人对象
     * @param authToken 认证 Token
     * @return 操作是否成功
     * @throws RuntimeException 如果 API 调用失败，会抛出包含错误详情的异常
     */
    public boolean addCandidate(Candidate candidate, String authToken) {
        System.out.println("HRService: 正在新增候选人: " + candidate.getName());
        try {
            // 使用 sendRequest 发送 POST 请求
            ServiceUtil.sendRequest(
                    CANDIDATE_ENDPOINT,
                    authToken,
                    candidate,
                    "POST",
                    new TypeReference<Void>() {} // 不期望返回对象
            );
            return true;
        } catch (Exception e) {
            System.err.println("API调用失败：无法新增候选人。");
            // 抛出异常让调用者知道具体错误信息，方便调试
            throw new RuntimeException("API 调用失败: 无法新增候选人。", e);
        }
    }

    // 🌟 新增方法 3: 处理候选人结果
    /**
     * 处理候选人结果 (PUT /api/candidates/{CandID}/result)。
     * @param candID 候选人 ID
     * @param result 结果值，例如 "录用", "淘汰"
     * @param authToken 认证 Token
     * @return 操作是否成功
     */
    public boolean updateCandidateResult(int candID, String result, String authToken) {
        System.out.println("HRService: 正在处理候选人结果 (ID: " + candID + ") 为: " + result);
        try {
            String endpoint = CANDIDATE_ENDPOINT + "/" + candID + "/result";

            // 构造符合 API 要求的 JSON 负载 {"result": "录用"}
            Map<String, String> payload = new HashMap<>();
            payload.put("result", result);

            // 使用 sendRequest 发送 PUT 请求
            ServiceUtil.sendRequest(
                    endpoint,
                    authToken,
                    payload,
                    "PUT",
                    new TypeReference<Void>() {} // 不期望返回对象
            );
            return true;
        } catch (Exception e) {
            System.err.println("API调用失败：无法更新候选人结果。");
            e.printStackTrace();
            return false;
        }
    }
}