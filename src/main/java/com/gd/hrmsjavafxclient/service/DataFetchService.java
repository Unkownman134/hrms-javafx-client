package com.gd.hrmsjavafxclient.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gd.hrmsjavafxclient.model.Employee;
import com.gd.hrmsjavafxclient.model.Position;
import com.gd.hrmsjavafxclient.model.User;
import com.gd.hrmsjavafxclient.model.Department;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.TimeUnit;

public class DataFetchService {

    private static final String BASE_URL = "http://localhost:8080/api";
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(java.time.Duration.ofSeconds(5))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DataFetchService() {
        objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * 新增：通过 Token 获取用户基础信息（如 UserId, RoleId, EmpId）。
     * @param authToken 认证Token (JWT 字符串)
     * @return User 对象
     */
    public User getUserByToken(String authToken) throws Exception {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/auth/user-details"))
                .header("Authorization", "Bearer " + authToken)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), User.class);
        } else {
            System.err.println("验证 Token 失败，状态码: " + response.statusCode());
            throw new RuntimeException("Token 验证失败，请重新登录。");
        }
    }

    /**
     * 查询员工档案信息（需要认证 Token）
     * 修正：增加 authToken 参数
     */
    public Employee getEmployeeById(Integer empId, String authToken) throws Exception {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/employees/" + empId))
                .header("Authorization", "Bearer " + authToken)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), Employee.class);
        } else {
            System.err.println("查询员工失败，EmpID: " + empId + ", 状态码: " + response.statusCode());
            return null;
        }
    }

    /**
     * 查询职位信息（需要认证 Token）
     * 修正：增加 authToken 参数
     */
    public Position getPositionById(Integer posId, String authToken) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/positions/" + posId))
                .header("Authorization", "Bearer " + authToken)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), Position.class);
        } else {
            System.err.println("查询职位失败，PosID: " + posId + ", 状态码: " + response.statusCode());
            return null;
        }
    }

    /**
     * 查询部门信息（需要认证 Token）
     * 新增：查询部门信息的方法
     */
    public Department getDepartmentById(Integer deptId, String authToken) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/departments/" + deptId))
                .header("Authorization", "Bearer " + authToken)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), Department.class);
        } else {
            System.err.println("查询部门失败，DeptID: " + deptId + ", 状态码: " + response.statusCode());
            return null;
        }
    }
}