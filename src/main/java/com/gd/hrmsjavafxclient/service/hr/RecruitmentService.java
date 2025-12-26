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


    public boolean hireCandidate(int candId, Employee emp, String username, String password, String token) {
        try {
            Optional<Employee> result = ServiceUtil.sendRequest(EMPLOYEE_ENDPOINT, token, emp, "POST", new TypeReference<Employee>() {});

            Integer generatedEmpId = null;

            if (result.isPresent()) {
                generatedEmpId = result.get().getEmpId();
            } else {
                generatedEmpId = findEmpIdByPhone(emp.getPhone(), token);
            }

            if (generatedEmpId == null) {
                throw new RuntimeException("无法获取新创建员工的 ID，请检查后端返回格式");
            }

            User newUser = new User();
            newUser.setUsername(username);
            newUser.setRawPassword(password);
            newUser.setEmpId(generatedEmpId);
            newUser.setRoleId(3);

            ServiceUtil.sendRequest(USER_ENDPOINT, token, newUser, "POST", new TypeReference<Void>() {});

            updateCandidateResult(candId, "录用", token);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


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