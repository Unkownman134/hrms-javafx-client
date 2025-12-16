package com.gd.hrmsjavafxclient.service.hr;

import com.fasterxml.jackson.core.type.TypeReference;
import com.gd.hrmsjavafxclient.model.*;
import com.gd.hrmsjavafxclient.util.ServiceUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class HRDataService {
    private static final String EMPLOYEE_ENDPOINT = "/employees";
    private static final String USER_ENDPOINT = "/users";
    private static final String DEPARTMENT_ENDPOINT = "/departments";
    private static final String POSITION_ENDPOINT = "/positions";
    private static final String CANDIDATE_ENDPOINT = "/candidates";

    public List<Candidate> getAllCandidates(String token) throws IOException, InterruptedException {
        return ServiceUtil.sendGet(CANDIDATE_ENDPOINT, token, new TypeReference<List<Candidate>>() {}).orElse(List.of());
    }

    public List<Position> getAllPositions(String token) throws IOException, InterruptedException {
        return ServiceUtil.sendGet(POSITION_ENDPOINT, token, new TypeReference<List<Position>>() {}).orElse(List.of());
    }

    public List<Department> getAllDepartments(String token) throws IOException, InterruptedException {
        return ServiceUtil.sendGet(DEPARTMENT_ENDPOINT, token, new TypeReference<List<Department>>() {}).orElse(List.of());
    }

    public List<Employee> getAllEmployees(String token) throws IOException, InterruptedException {
        return ServiceUtil.sendGet(EMPLOYEE_ENDPOINT, token, new TypeReference<List<Employee>>() {}).orElse(List.of());
    }

    public boolean addCandidate(Candidate c, String token) {
        System.out.println("[DEBUG] 尝试新增候选人: " + c.getName() + ", 性别: " + c.getGender());
        try {
            ServiceUtil.sendRequest(CANDIDATE_ENDPOINT, token, c, "POST", new TypeReference<Void>() {});
            return true;
        } catch (Exception e) {
            System.err.println("[DEBUG] 新增候选人失败: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public boolean updateCandidateResult(int id, String res, String token) {
        System.out.println("[DEBUG] 更新结果请求: ID=" + id + ", Result=" + res);
        try {
            Map<String, String> p = new HashMap<>(); p.put("result", res);
            ServiceUtil.sendRequest(CANDIDATE_ENDPOINT + "/" + id + "/result", token, p, "PUT", new TypeReference<Void>() {});
            return true;
        } catch (Exception e) { return false; }
    }

    public Optional<Employee> createEmployee(Employee emp, String token) {
        try {
            // 1. 发送请求
            ServiceUtil.sendRequest(EMPLOYEE_ENDPOINT, token, emp, "POST", new TypeReference<String>() {});

            List<Employee> all = getAllEmployees(token);
            return all.stream()
                    .filter(e -> e.getPhone().equals(emp.getPhone()))
                    .findFirst();

        } catch (Exception e) {
            System.err.println("[DEBUG] 创建员工异常: " + e.getMessage());
            return Optional.empty();
        }
    }

    public boolean createUser(User user, String token) {
        try {
            ServiceUtil.sendRequest(USER_ENDPOINT, token, user, "POST", new TypeReference<Void>() {});
            return true;
        } catch (Exception e) { return false; }
    }
}