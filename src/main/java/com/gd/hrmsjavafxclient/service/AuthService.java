package com.gd.hrmsjavafxclient.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gd.hrmsjavafxclient.model.LoginRequest;
import com.gd.hrmsjavafxclient.model.User;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class AuthService {

    // ⚠️ 确保 BASE_URL 正确指向你的 Spring Boot 后端
    private static final String BASE_URL = "http://localhost:8080/api";
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public User login(String username, String password) throws Exception {
        LoginRequest loginRequest = new LoginRequest(username, password);
        String requestBody = objectMapper.writeValueAsString(loginRequest);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            // 登录成功，返回 User 对象
            return objectMapper.readValue(response.body(), User.class);
        } else if (response.statusCode() == 401) {
            // 用户名或密码错误
            System.err.println("登录失败，状态码: 401, 错误信息: 用户名或密码错误");
            return null;
        } else {
            // 其他服务器错误
            System.err.println("登录时发生服务器错误，状态码: " + response.statusCode());
            throw new RuntimeException("服务器返回错误：" + response.body());
        }
    }
}