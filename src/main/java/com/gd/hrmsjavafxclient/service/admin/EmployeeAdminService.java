package com.gd.hrmsjavafxclient.service.admin;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule; // 🌟 重点：处理 LocalDate
import com.gd.hrmsjavafxclient.model.Employee;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

/**
 * 客户端 R10: 员工档案管理服务 (CRUD)
 */
public class EmployeeAdminService {

    private static final String BASE_URL = "http://localhost:8080/api/employees";
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper;

    public EmployeeAdminService() {
        // 注册 JavaTimeModule 以正确处理 LocalDate 类型
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    // --- 1. 查询所有 (R) ---
    public List<Employee> getAllEmployees() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<List<Employee>>() {});
        } else {
            throw new RuntimeException("查询员工档案失败，状态码: " + response.statusCode());
        }
    }

    // --- 2. 创建员工 (C) ---
    public Employee createEmployee(Employee newEmployee) throws Exception {
        String requestBody = objectMapper.writeValueAsString(newEmployee);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 201) { // 后端返回 201 Created
            return objectMapper.readValue(response.body(), Employee.class);
        } else {
            throw new RuntimeException("创建员工失败: " + response.body());
        }
    }

    // --- 3. 更新员工 (U) ---
    public Employee updateEmployee(Integer empId, Employee employeeDetails) throws Exception {
        String requestBody = objectMapper.writeValueAsString(employeeDetails);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + empId))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            // 成功返回的是字符串，这里我们只返回一个成功 Employee 实例
            Employee updatedEmployee = new Employee();
            updatedEmployee.setEmpId(empId);
            return updatedEmployee;
        } else if (response.statusCode() == 404) {
            throw new RuntimeException("更新失败: 未找到员工 ID " + empId);
        } else {
            throw new RuntimeException("更新员工失败: " + response.body());
        }
    }

    // --- 4. 删除员工 (D) ---
    public void deleteEmployee(Integer empId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + empId))
                .DELETE()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("删除员工失败: " + response.body());
        }
    }
}