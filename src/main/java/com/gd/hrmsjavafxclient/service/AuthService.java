package com.gd.hrmsjavafxclient.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gd.hrmsjavafxclient.dto.LoginRequest;
import com.gd.hrmsjavafxclient.model.User;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class AuthService {
    // 确保 BASE_URL 匹配你的 Spring Boot 后端地址！
    private static final String BASE_URL = "http://localhost:8080/api/auth";

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public User login(String username, String password) throws Exception {
        LoginRequest requestBody = new LoginRequest(username, password);
        // Java 对象 -> JSON 字符串
        String jsonBody = objectMapper.writeValueAsString(requestBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/login")) // 目标地址 P7
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        // 发送请求并获取响应
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            // 登录成功，JSON 字符串 -> User 对象
            return objectMapper.readValue(response.body(), User.class);
        } else {
            // 登录失败或服务器错误
            System.err.println("登录失败，状态码: " + response.statusCode() + ", 错误信息: " + response.body());
            return null;
        }
    }
}