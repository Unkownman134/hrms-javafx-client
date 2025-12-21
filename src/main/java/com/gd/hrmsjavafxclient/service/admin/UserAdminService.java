package com.gd.hrmsjavafxclient.service.admin;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gd.hrmsjavafxclient.model.User;
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

    public User createUser(User user) throws Exception {
        String body = objectMapper.writeValueAsString(user);
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL)).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(body)).build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return objectMapper.readValue(response.body(), User.class);
    }

    public User updateUser(Integer userId, User user) throws Exception {
        String body = objectMapper.writeValueAsString(user);
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/" + userId)).header("Content-Type", "application/json").PUT(HttpRequest.BodyPublishers.ofString(body)).build();
        httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return user;
    }

    public void deleteUser(Integer userId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/" + userId)).DELETE().build();
        httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
}