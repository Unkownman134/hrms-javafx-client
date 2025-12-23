package com.gd.hrmsjavafxclient.service.hr;

import com.fasterxml.jackson.core.type.TypeReference;
import com.gd.hrmsjavafxclient.model.*;
import com.gd.hrmsjavafxclient.util.ServiceUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * HR 模块数据服务类 - 终极整合版
 * 负责与后端 API 进行数据交互，支持 HR 模块所有子功能 (oﾟvﾟ)ノ
 */
public class HRDataService {
    private static final String EMPLOYEE_ENDPOINT = "/employees";
    private static final String DEPARTMENT_ENDPOINT = "/departments";
    private static final String POSITION_ENDPOINT = "/positions";
    private static final String CANDIDATE_ENDPOINT = "/candidates";
    private static final String SALARY_ENDPOINT = "/salary/standards";
    private static final String SHIFT_ENDPOINT = "/shift/rules";

    // --- 员工管理相关 (兼容 EmployeeController) ---
    public List<Employee> getAllEmployees(String token) {
        try {
            return ServiceUtil.sendGet(EMPLOYEE_ENDPOINT, token, new TypeReference<List<Employee>>() {}).orElse(List.of());
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public Optional<Employee> createEmployee(Employee emp, String token) {
        try {
            return ServiceUtil.sendRequest(EMPLOYEE_ENDPOINT, token, emp, "POST", new TypeReference<Employee>() {});
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public boolean updateEmployee(Employee emp, String token) {
        try {
            ServiceUtil.sendRequest(EMPLOYEE_ENDPOINT + "/" + emp.getEmpId(), token, emp, "PUT", new TypeReference<Void>() {});
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteEmployee(int empId, String token) {
        try {
            ServiceUtil.sendRequest(EMPLOYEE_ENDPOINT + "/" + empId, token, null, "DELETE", new TypeReference<Void>() {});
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- 部门管理相关 (兼容 DepartmentController) ---
    public List<Department> getAllDepartments(String token) {
        try {
            return ServiceUtil.sendGet(DEPARTMENT_ENDPOINT, token, new TypeReference<List<Department>>() {}).orElse(List.of());
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public boolean addDepartment(Department department, String token) {
        try {
            ServiceUtil.sendRequest(DEPARTMENT_ENDPOINT, token, department, "POST", new TypeReference<Void>() {});
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateDepartment(Department department, String token) {
        try {
            ServiceUtil.sendRequest(DEPARTMENT_ENDPOINT + "/" + department.getDeptId(), token, department, "PUT", new TypeReference<Void>() {});
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteDepartment(int deptId, String token) {
        try {
            ServiceUtil.sendRequest(DEPARTMENT_ENDPOINT + "/" + deptId, token, null, "DELETE", new TypeReference<Void>() {});
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- 职位体系相关 (兼容 PositionController) ---
    public List<Position> getAllPositions(String token) {
        try {
            return ServiceUtil.sendGet(POSITION_ENDPOINT, token, new TypeReference<List<Position>>() {}).orElse(List.of());
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public boolean addPosition(Position position, String token) {
        try {
            ServiceUtil.sendRequest(POSITION_ENDPOINT, token, position, "POST", new TypeReference<Void>() {});
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updatePosition(Position position, String token) {
        try {
            ServiceUtil.sendRequest(POSITION_ENDPOINT + "/" + position.getPosId(), token, position, "PUT", new TypeReference<Void>() {});
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deletePosition(int posId, String token) {
        try {
            ServiceUtil.sendRequest(POSITION_ENDPOINT + "/" + posId, token, null, "DELETE", new TypeReference<Void>() {});
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- 候选人/招聘管理相关 (兼容 RecruitmentController) ---
    public List<Candidate> getAllCandidates(String token) {
        try {
            return ServiceUtil.sendGet(CANDIDATE_ENDPOINT, token, new TypeReference<List<Candidate>>() {}).orElse(List.of());
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
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

    // --- 薪资标准相关 ---
    public List<SalaryStandard> getAllSalaryStandards(String token) {
        try {
            return ServiceUtil.sendGet(SALARY_ENDPOINT, token, new TypeReference<List<SalaryStandard>>() {}).orElse(List.of());
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    // --- 班次管理相关 (🌟 特殊处理 POST 逻辑) ---
    public List<ShiftRule> getAllShiftRules(String token) {
        try {
            return ServiceUtil.sendGet(SHIFT_ENDPOINT, token, new TypeReference<List<ShiftRule>>() {}).orElse(List.of());
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * 关键修正：新增班次规则
     * 只有这里使用 Map 过滤掉 ruleId，确保后端校验通过
     */
    public boolean addShiftRule(ShiftRule rule, String token) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("ruleName", rule.getRuleName());
            payload.put("workStartTime", rule.getWorkStartTime());
            payload.put("workEndTime", rule.getWorkEndTime());
            payload.put("lateToleranceMin", rule.getLateToleranceMin());

            ServiceUtil.sendRequest(SHIFT_ENDPOINT, token, payload, "POST", new TypeReference<Void>() {});
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateShiftRule(ShiftRule rule, String token) {
        try {
            ServiceUtil.sendRequest(SHIFT_ENDPOINT + "/" + rule.getRuleId(), token, rule, "PUT", new TypeReference<Void>() {});
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteShiftRule(int ruleId, String token) {
        try {
            ServiceUtil.sendRequest(SHIFT_ENDPOINT + "/" + ruleId, token, null, "DELETE", new TypeReference<Void>() {});
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}