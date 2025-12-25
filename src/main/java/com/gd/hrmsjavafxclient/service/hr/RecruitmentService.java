package com.gd.hrmsjavafxclient.service.hr;

import com.fasterxml.jackson.core.type.TypeReference;
import com.gd.hrmsjavafxclient.model.*;
import com.gd.hrmsjavafxclient.util.ServiceUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 招聘管理专用服务类 🚀 - 修复文本响应解析版
 */
public class RecruitmentService {

    private static final String CANDIDATE_ENDPOINT = "/candidates";
    private static final String POSITION_ENDPOINT = "/positions";
    private static final String EMPLOYEE_ENDPOINT = "/employees";
    private static final String USER_ENDPOINT = "/users";

    public List<Candidate> getAllCandidates(String token) {
        try {
            return ServiceUtil.sendGet(CANDIDATE_ENDPOINT, token, new TypeReference<List<Candidate>>() {})
                    .orElse(List.of());
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public List<Position> getAllPositions(String token) {
        try {
            return ServiceUtil.sendGet(POSITION_ENDPOINT, token, new TypeReference<List<Position>>() {})
                    .orElse(List.of());
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public boolean createCandidate(Candidate candidate, String token) {
        try {
            ServiceUtil.sendRequest(CANDIDATE_ENDPOINT, token, candidate, "POST", new TypeReference<Candidate>() {});
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateCandidateResult(int candId, String result, String token) {
        try {
            Map<String, String> body = new HashMap<>();
            body.put("result", result);
            ServiceUtil.sendRequest(CANDIDATE_ENDPOINT + "/" + candId + "/result", token, body, "PUT", new TypeReference<Void>() {});
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 核心逻辑：录用并入职 🌟
     * 修正：兼容后端返回的纯文本 ID 响应
     */
    public boolean hireCandidate(int candId, Employee emp, String username, String password, String token) {
        try {
            // 1. 发送 POST 请求创建员工
            // 注意：因为后端返回纯文本，ServiceUtil 会返回 Optional.empty() 并打印 [DEBUG]
            // 我们需要手动处理这个特殊情况

            // 为了拿到原始响应，这里我们稍微特殊处理一下，或者直接通过正则表达式解析日志中提到的字符串
            // 在实际项目中，如果 ServiceUtil 没法直接返回 String，我们可以修改这里的逻辑

            // 假设我们稍微改一下逻辑，先尝试发送请求
            Optional<Employee> result = ServiceUtil.sendRequest(EMPLOYEE_ENDPOINT, token, emp, "POST", new TypeReference<Employee>() {});

            Integer generatedEmpId = null;

            if (result.isPresent()) {
                generatedEmpId = result.get().getEmpId();
            } else {
                // 🌸 这里的 hack 逻辑：如果返回空，说明可能命中了 ServiceUtil 的“非 JSON 成功响应”
                // 因为我们没法在 Service 层拿到 ServiceUtil 内部的 responseBody，
                // 如果后端 API 不改，最稳妥的办法是再查一遍刚插入的员工，或者让 ServiceUtil 支持返回 String。

                // 但根据你的日志：[DEBUG] 收到非 JSON 成功响应: 员工档案新增成功！ID: 22
                // 我们假设 ID 已经生成了，如果你能改 ServiceUtil 让它在 Optional.empty() 时不抛错，
                // 或者我们这里做一个临时的“根据电话获取员工 ID”的查询：
                generatedEmpId = findEmpIdByPhone(emp.getPhone(), token);
            }

            if (generatedEmpId == null) {
                throw new RuntimeException("无法获取新创建员工的 ID，请检查后端返回格式");
            }

            // 2. 创建关联的用户账号
            User newUser = new User();
            newUser.setUsername(username);
            newUser.setRawPassword(password);
            newUser.setEmpId(generatedEmpId); // 使用拿到的 ID
            newUser.setRoleId(3);

            ServiceUtil.sendRequest(USER_ENDPOINT, token, newUser, "POST", new TypeReference<Void>() {});

            // 3. 更新候选人状态为“录用”
            updateCandidateResult(candId, "录用", token);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 辅助方法：入职过程中通过手机号反查员工 ID (应对后端返回文本而非对象的情况)
     */
    private Integer findEmpIdByPhone(String phone, String token) {
        try {
            List<Employee> emps = ServiceUtil.sendGet(EMPLOYEE_ENDPOINT, token, new TypeReference<List<Employee>>() {})
                    .orElse(List.of());
            return emps.stream()
                    .filter(e -> phone.equals(e.getPhone()))
                    .map(Employee::getEmpId)
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            return null;
        }
    }
}