package com.gd.hrmsjavafxclient.service.admin;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gd.hrmsjavafxclient.model.Department;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

/**
 * 客户端 R1: 部门信息管理服务 (CRUD)
 */
public class DepartmentAdminService {

    private static final String BASE_URL = "http://localhost:8080/api/departments";
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // --- 1. 查询所有 (R) ---
    public List<Department> getAllDepartments() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<List<Department>>() {});
        } else {
            throw new RuntimeException("查询部门信息失败，状态码: " + response.statusCode());
        }
    }

    // --- 2. 创建部门 (C) ---
    public Department createDepartment(Department newDepartment) throws Exception {
        String requestBody = objectMapper.writeValueAsString(newDepartment);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 201) { // 后端返回 201 Created
            return objectMapper.readValue(response.body(), Department.class);
        } else {
            throw new RuntimeException("创建部门失败: " + response.body());
        }
    }

    // --- 3. 更新部门 (U) ---
    public Department updateDepartment(Integer deptId, Department departmentDetails) throws Exception {
        // 更新时只需要 DeptName
        Department updatePayload = new Department();
        updatePayload.setDeptName(departmentDetails.getDeptName());

        String requestBody = objectMapper.writeValueAsString(updatePayload);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + deptId))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            departmentDetails.setDeptId(deptId); // 成功，返回带有 ID 的对象
            return departmentDetails;
        } else if (response.statusCode() == 404) {
            throw new RuntimeException("更新失败: 未找到部门 ID " + deptId);
        } else {
            throw new RuntimeException("更新部门失败: " + response.body());
        }
    }

    // --- 4. 删除部门 (D) ---
    public void deleteDepartment(Integer deptId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + deptId))
                .DELETE()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("删除部门失败: " + response.body());
        }
    }
}