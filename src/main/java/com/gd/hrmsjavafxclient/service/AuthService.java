package com.gd.hrmsjavafxclient.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gd.hrmsjavafxclient.model.LoginRequest;
import com.gd.hrmsjavafxclient.model.User;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class AuthService {

    private static final String BASE_URL = "http://localhost:8080/api";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
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
            String token = response.body();
            if (token != null) {
                token = token.trim();
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
                .header("Authorization", "Bearer " + authToken)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), User.class);
        } else {
            System.err.println("验证用户登录失败，状态码: " + response.statusCode());
            return null;
        }
    }
}