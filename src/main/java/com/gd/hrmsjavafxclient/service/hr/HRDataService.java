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

    // --- 查询方法 ---
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

    // --- 员工管理 (Employee) ---
    public Optional<Employee> createEmployee(Employee emp, String token) {
        try {
            ServiceUtil.sendRequest(EMPLOYEE_ENDPOINT, token, emp, "POST", new TypeReference<String>() {});
            List<Employee> all = getAllEmployees(token);
            return all.stream().filter(e -> e.getPhone().equals(emp.getPhone())).findFirst();
        } catch (Exception e) {
            System.err.println("[DEBUG] 创建员工异常: " + e.getMessage());
            return Optional.empty();
        }
    }

    public boolean updateEmployee(Employee emp, String token) {
        try {
            ServiceUtil.sendRequest(EMPLOYEE_ENDPOINT + "/" + emp.getEmpId(), token, emp, "PUT", new TypeReference<Void>() {});
            return true;
        } catch (Exception e) { return false; }
    }

    public boolean deleteEmployee(int empId, String token) {
        try {
            ServiceUtil.sendRequest(EMPLOYEE_ENDPOINT + "/" + empId, token, null, "DELETE", new TypeReference<Void>() {});
            return true;
        } catch (Exception e) { return false; }
    }

    // --- 部门管理 (Department) ---
    public boolean addDepartment(Department dept, String token) {
        try {
            ServiceUtil.sendRequest(DEPARTMENT_ENDPOINT, token, dept, "POST", new TypeReference<Void>() {});
            return true;
        } catch (Exception e) { return false; }
    }

    public boolean updateDepartment(Department dept, String token) {
        try {
            ServiceUtil.sendRequest(DEPARTMENT_ENDPOINT + "/" + dept.getDeptId(), token, dept, "PUT", new TypeReference<Void>() {});
            return true;
        } catch (Exception e) { return false; }
    }

    public boolean deleteDepartment(int deptId, String token) {
        try {
            ServiceUtil.sendRequest(DEPARTMENT_ENDPOINT + "/" + deptId, token, null, "DELETE", new TypeReference<Void>() {});
            return true;
        } catch (Exception e) { return false; }
    }

    // --- 职位管理 (Position) ---
    public boolean addPosition(Position pos, String token) {
        try {
            ServiceUtil.sendRequest(POSITION_ENDPOINT, token, pos, "POST", new TypeReference<Void>() {});
            return true;
        } catch (Exception e) { return false; }
    }

    public boolean updatePosition(Position pos, String token) {
        try {
            ServiceUtil.sendRequest(POSITION_ENDPOINT + "/" + pos.getPosId(), token, pos, "PUT", new TypeReference<Void>() {});
            return true;
        } catch (Exception e) { return false; }
    }

    public boolean deletePosition(int posId, String token) {
        try {
            ServiceUtil.sendRequest(POSITION_ENDPOINT + "/" + posId, token, null, "DELETE", new TypeReference<Void>() {});
            return true;
        } catch (Exception e) { return false; }
    }

    // --- 候选人与结果更新 ---
    public boolean addCandidate(Candidate c, String token) {
        try {
            ServiceUtil.sendRequest(CANDIDATE_ENDPOINT, token, c, "POST", new TypeReference<Void>() {});
            return true;
        } catch (Exception e) { return false; }
    }

    public boolean updateCandidateResult(int id, String res, String token) {
        try {
            Map<String, String> p = new HashMap<>(); p.put("result", res);
            ServiceUtil.sendRequest(CANDIDATE_ENDPOINT + "/" + id + "/result", token, p, "PUT", new TypeReference<Void>() {});
            return true;
        } catch (Exception e) { return false; }
    }

    public boolean createUser(User user, String token) {
        try {
            ServiceUtil.sendRequest(USER_ENDPOINT, token, user, "POST", new TypeReference<Void>() {});
            return true;
        } catch (Exception e) { return false; }
    }
}