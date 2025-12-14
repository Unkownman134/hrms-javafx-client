package com.gd.hrmsjavafxclient.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gd.hrmsjavafxclient.model.LoginRequest;
import com.gd.hrmsjavafxclient.model.User; // 假设 User 模型存在
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class AuthService {

    // ⚠️ 确保 BASE_URL 正确指向你的 Spring Boot 后端
    private static final String BASE_URL = "http://localhost:8080/api";

    // 使用短连接超时，避免登录界面卡死
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5)) // 使用 Duration.ofSeconds(5) 配置超时
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 登录认证，返回 JWT Token 字符串。
     * @param username 用户名
     * @param password 密码
     * @return 认证成功的 JWT Token 字符串，失败返回 null。
     */
    public String login(String username, String password) throws Exception {
        LoginRequest loginRequest = new LoginRequest(username, password);
        String requestBody = objectMapper.writeValueAsString(loginRequest);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            // 后端现在返回的是 JWT Token 字符串
            String token = response.body();
            // 🚨 关键修正：去除前后可能的空格和引号，确保是纯净的 JWT 字符串
            if (token != null) {
                token = token.trim();
                // 检查是否是被双引号包裹的JSON字符串（例如: "eyJ..."）
                if (token.startsWith("\"") && token.endsWith("\"") && token.length() > 1) {
                    token = token.substring(1, token.length() - 1);
                }
            }
            return token;

        } else if (response.statusCode() == 401) {
            System.err.println("登录失败，状态码: 401, 错误信息: 用户名或密码错误");
            return null;
        } else {
            System.err.println("登录失败，状态码: " + response.statusCode() + ", 响应: " + response.body());
            // 抛出运行时异常，让 Controller 层处理网络或其他错误
            throw new RuntimeException("登录 API 响应异常，状态码: " + response.statusCode());
        }
    }

    /**
     * 通过 Token 获取用户基础信息（如 UserId, RoleId, EmpId）。
     * @param authToken 认证Token (JWT 字符串)
     * @return User 对象
     */
    public User getUserDetails(String authToken) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/auth/user-details"))
                // 确保 Token 格式是 Bearer
                .header("Authorization", "Bearer " + authToken)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            // 假设后端返回的是完整的 User JSON 对象
            return objectMapper.readValue(response.body(), User.class);
        } else {
            System.err.println("验证用户登录失败，状态码: " + response.statusCode());
            // Token 过期或无效会导致 401/403，返回 null 强制重新登录
            return null;
        }
    }
}