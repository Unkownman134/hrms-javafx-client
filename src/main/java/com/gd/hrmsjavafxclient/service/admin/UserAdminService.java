package com.gd.hrmsjavafxclient.service.admin;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gd.hrmsjavafxclient.model.User;
import com.gd.hrmsjavafxclient.model.Employee; // 🌟 记得引入 Employee 模型
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class UserAdminService {
    private static final String BASE_URL = "http://localhost:8080/api/users";
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<User> getAllUsers() throws Exception {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL)).GET().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<List<User>>() {});
        }
        throw new RuntimeException("获取用户失败: " + response.statusCode());
    }

    public List<Employee> getAllEmployees() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/employees"))
                .GET().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            ObjectMapper empMapper = new ObjectMapper();
            empMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
            return empMapper.readValue(response.body(), new TypeReference<List<Employee>>() {});
        }
        throw new RuntimeException("获取员工列表失败");
    }

    public User createUser(User user) throws Exception {
        String body = objectMapper.writeValueAsString(user);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200 || response.statusCode() == 201) {
            String respBody = response.body();
            if (respBody != null && respBody.trim().startsWith("{")) {
                return objectMapper.readValue(respBody, User.class);
            }
            return user;
        } else {
            throw new RuntimeException("创建失败: " + response.body());
        }
    }

    public User updateUser(Integer userId, User user) throws Exception {
        String body = objectMapper.writeValueAsString(user);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + userId))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return user;
        } else {
            throw new RuntimeException("更新失败: " + response.body());
        }
    }

    public void deleteUser(Integer userId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/" + userId)).DELETE().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException("删除失败: " + response.body());
        }
    }
}