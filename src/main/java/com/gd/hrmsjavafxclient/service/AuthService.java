package com.gd.hrmsjavafxclient.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gd.hrmsjavafxclient.model.LoginRequest;
import com.gd.hrmsjavafxclient.model.User; // 依然保留，虽然 login 不直接返回它
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class AuthService {

    // ⚠️ 确保 BASE_URL 正确指向你的 Spring Boot 后端
    private static final String BASE_URL = "http://localhost:8080/api";
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 登录认证，返回 JWT Token 字符串。
     * 🌟 修正：返回类型为 String，对应后端返回的 JWT 字符串。
     * @param username 用户名
     * @param password 密码
     * @return 认证成功的 JWT Token 字符串，失败返回 null。
     */
    public String login(String username, String password) throws Exception { // 👈 修正：返回类型改为 String
        LoginRequest loginRequest = new LoginRequest(username, password);
        String requestBody = objectMapper.writeValueAsString(loginRequest);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            // 🌟 关键修正：后端现在返回的是 JWT Token 字符串，直接返回响应体
            return response.body();
        } else if (response.statusCode() == 401) {
            System.err.println("登录失败，状态码: 401, 错误信息: 用户名或密码错误");
            return null;
        } else {
            System.err.println("登录失败，状态码: " + response.statusCode() + ", 响应: " + response.body());
            throw new Exception("登录失败，状态码: " + response.statusCode() + ", 响应: " + response.body());
        }
    }
}