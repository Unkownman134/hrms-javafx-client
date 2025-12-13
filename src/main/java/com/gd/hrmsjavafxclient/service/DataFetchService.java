package com.gd.hrmsjavafxclient.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gd.hrmsjavafxclient.model.Employee;
import com.gd.hrmsjavafxclient.model.Position;
import com.gd.hrmsjavafxclient.model.User; // 导入 User model
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class DataFetchService {

    private static final String BASE_URL = "http://localhost:8080/api";
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DataFetchService() {
        // 注册 Java 8 日期时间模块，解决 LocalDate/LocalDateTime 无法反序列化的问题
        objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * 🌟 新增：通过 Token 获取用户基础信息（如 UserId, RoleId, EmpId）。
     * @param authToken 认证Token (JWT 字符串)
     * @return User 对象
     */
    public User getUserByToken(String authToken) throws Exception { // 👈 参数类型是 String

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/auth/user-details"))
                // 🌟 关键修正：发送标准的 Authorization: Bearer <JWT> Header
                .header("Authorization", "Bearer " + authToken)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), User.class);
        } else {
            System.err.println("查询用户基础信息失败，状态码: " + response.statusCode());
            throw new RuntimeException("无法获取用户基本信息，状态码: " + response.statusCode());
        }
    }

    /**
     * 查询员工信息（需要认证 Token）
     * 之前报错：应为 2 个实参，但实际为 1 个
     * 🌟 修正：增加 authToken 参数
     */
    public Employee getEmployeeById(Integer empId, String authToken) throws Exception { // 👈 增加 authToken 参数
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/employees/" + empId))
                .header("Authorization", "Bearer " + authToken) // 👈 增加 Token 头
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
     * 之前报错：应为 2 个实参，但实际为 1 个
     * 🌟 修正：增加 authToken 参数
     */
    public Position getPositionById(Integer posId, String authToken) throws Exception { // 👈 增加 authToken 参数
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/positions/" + posId))
                .header("Authorization", "Bearer " + authToken) // 👈 增加 Token 头
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
}