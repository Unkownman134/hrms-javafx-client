package com.gd.hrmsjavafxclient.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gd.hrmsjavafxclient.model.User;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

/**
 * 客户端 R11: 用户账号信息管理服务
 */
public class UserAdminService {

    private static final String BASE_URL = "http://localhost:8080/api/users";
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // --- 1. 查询所有 (R) ---
    public List<User> getAllUsers() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            // 注意 TypeReference 用于反序列化 List<User>
            return objectMapper.readValue(response.body(), new TypeReference<List<User>>() {});
        } else {
            throw new RuntimeException("查询用户失败，状态码: " + response.statusCode());
        }
    }

    // --- 2. 创建用户 (C) ---
    public User createUser(User newUser) throws Exception {
        String requestBody = objectMapper.writeValueAsString(newUser);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), User.class);
        } else {
            // 后端可能返回 400 错误信息
            throw new RuntimeException("创建用户失败: " + response.body());
        }
    }

    // --- 3. 更新用户 (U) ---
    public User updateUser(Integer userId, User userDetails) throws Exception {
        String requestBody = objectMapper.writeValueAsString(userDetails);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + userId))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            // 成功返回的是字符串，我们只返回一个成功 User 实例，方便后续刷新表格
            User updatedUser = new User();
            updatedUser.setUserId(userId);
            return updatedUser;
        } else if (response.statusCode() == 404) {
            throw new RuntimeException("更新失败: 未找到用户 ID " + userId);
        } else {
            throw new RuntimeException("更新用户失败: " + response.body());
        }
    }

    // --- 4. 删除用户 (D) ---
    public void deleteUser(Integer userId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + userId))
                .DELETE()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            // 后端删除成功返回 200，否则返回错误信息
            throw new RuntimeException("删除用户失败: " + response.body());
        }
    }
}