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
    private static final String SALARY_ENDPOINT = "/salary/standards";

    public List<SalaryStandard> getAllSalaryStandards(String token) {
        try {
            return ServiceUtil.sendGet(SALARY_ENDPOINT, token, new TypeReference<List<SalaryStandard>>() {}).orElse(List.of());
        } catch (Exception e) { return List.of(); }
    }

    public boolean addSalaryStandard(SalaryStandard std, String token) {
        try {
            ServiceUtil.sendRequest(SALARY_ENDPOINT, token, std, "POST", new TypeReference<Void>() {});
            return true;
        } catch (Exception e) { return false; }
    }

    public boolean updateSalaryStandard(SalaryStandard std, String token) {
        try {
            ServiceUtil.sendRequest(SALARY_ENDPOINT + "/" + std.getStdId(), token, std, "PUT", new TypeReference<Void>() {});
            return true;
        } catch (Exception e) { return false; }
    }

    public boolean deleteSalaryStandard(int stdId, String token) {
        try {
            ServiceUtil.sendRequest(SALARY_ENDPOINT + "/" + stdId, token, null, "DELETE", new TypeReference<Void>() {});
            return true;
        } catch (Exception e) { return false; }
    }

    // --- 原有查询方法 ---
    public List<Candidate> getAllCandidates(String token) throws IOException, InterruptedException {
        return ServiceUtil.sendGet(CANDIDATE_ENDPOINT, token, new TypeReference<List<Candidate>>() {}).orElse(List.of());
    }

    public List<Position> getAllPositions(String token) throws IOException, InterruptedException {
        return ServiceUtil.sendGet(POSITION_ENDPOINT, token, new TypeReference<List<Position>>() {}).orElse(List.of());
    }

    public List<Department> getAllDepartments(String token) throws IOException, InterruptedException {
        return ServiceUtil.sendGet(DEPARTMENT_ENDPOINT, token, new TypeReference<List<Department>>() {}).orElse(List.of());
    }

    public List<Employee> getAllEmployees(String token) {
        try {
            return ServiceUtil.sendGet(EMPLOYEE_ENDPOINT, token, new TypeReference<List<Employee>>() {}).orElse(List.of());
        } catch (Exception e) { return List.of(); }
    }

    // --- 原有增删改方法 ---
    public Optional<Employee> createEmployee(Employee emp, String token) {
        try { return ServiceUtil.sendRequest(EMPLOYEE_ENDPOINT, token, emp, "POST", new TypeReference<Employee>() {}); }
        catch (Exception e) { return Optional.empty(); }
    }

    public boolean updateEmployee(Employee emp, String token) {
        try { ServiceUtil.sendRequest(EMPLOYEE_ENDPOINT + "/" + emp.getEmpId(), token, emp, "PUT", new TypeReference<Void>() {}); return true; }
        catch (Exception e) { return false; }
    }

    public boolean deleteEmployee(int empId, String token) {
        try { ServiceUtil.sendRequest(EMPLOYEE_ENDPOINT + "/" + empId, token, null, "DELETE", new TypeReference<Void>() {}); return true; }
        catch (Exception e) { return false; }
    }

    public boolean addDepartment(Department dept, String token) {
        try { ServiceUtil.sendRequest(DEPARTMENT_ENDPOINT, token, dept, "POST", new TypeReference<Void>() {}); return true; }
        catch (Exception e) { return false; }
    }

    public boolean updateDepartment(Department dept, String token) {
        try { ServiceUtil.sendRequest(DEPARTMENT_ENDPOINT + "/" + dept.getDeptId(), token, dept, "PUT", new TypeReference<Void>() {}); return true; }
        catch (Exception e) { return false; }
    }

    public boolean deleteDepartment(int deptId, String token) {
        try { ServiceUtil.sendRequest(DEPARTMENT_ENDPOINT + "/" + deptId, token, null, "DELETE", new TypeReference<Void>() {}); return true; }
        catch (Exception e) { return false; }
    }

    public boolean addPosition(Position pos, String token) {
        try { ServiceUtil.sendRequest(POSITION_ENDPOINT, token, pos, "POST", new TypeReference<Void>() {}); return true; }
        catch (Exception e) { return false; }
    }

    public boolean updatePosition(Position pos, String token) {
        try { ServiceUtil.sendRequest(POSITION_ENDPOINT + "/" + pos.getPosId(), token, pos, "PUT", new TypeReference<Void>() {}); return true; }
        catch (Exception e) { return false; }
    }

    public boolean deletePosition(int posId, String token) {
        try { ServiceUtil.sendRequest(POSITION_ENDPOINT + "/" + posId, token, null, "DELETE", new TypeReference<Void>() {}); return true; }
        catch (Exception e) { return false; }
    }

    public boolean addCandidate(Candidate c, String token) {
        try { ServiceUtil.sendRequest(CANDIDATE_ENDPOINT, token, c, "POST", new TypeReference<Void>() {}); return true; }
        catch (Exception e) { return false; }
    }

    public boolean updateCandidateResult(int id, String res, String token) {
        try {
            Map<String, String> p = new HashMap<>(); p.put("result", res);
            ServiceUtil.sendRequest(CANDIDATE_ENDPOINT + "/" + id + "/result", token, p, "PUT", new TypeReference<Void>() {});
            return true;
        } catch (Exception e) { return false; }
    }

    public boolean createUser(User user, String token) {
        try { ServiceUtil.sendRequest(USER_ENDPOINT, token, user, "POST", new TypeReference<Void>() {}); return true; }
        catch (Exception e) { return false; }
    }
}