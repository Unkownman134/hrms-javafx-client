package com.gd.hrmsjavafxclient.service.admin;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gd.hrmsjavafxclient.model.Position;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

/**
 * 客户端 R9: 职位信息管理服务 (CRUD)
 */
public class PositionAdminService {

    private static final String BASE_URL = "http://localhost:8080/api/positions";
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // --- 1. 查询所有 (R) ---
    public List<Position> getAllPositions() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<List<Position>>() {});
        } else {
            throw new RuntimeException("查询职位失败，状态码: " + response.statusCode());
        }
    }

    // --- 2. 创建职位 (C) ---
    public Position createPosition(Position newPosition) throws Exception {
        String requestBody = objectMapper.writeValueAsString(newPosition);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            // 后端成功返回的是创建的 Position 对象
            return objectMapper.readValue(response.body(), Position.class);
        } else {
            throw new RuntimeException("创建职位失败: " + response.body());
        }
    }

    // --- 3. 更新职位 (U) ---
    public Position updatePosition(Integer posId, Position positionDetails) throws Exception {
        String requestBody = objectMapper.writeValueAsString(positionDetails);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + posId))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            // 成功返回的是字符串，我们只返回一个成功 Position 实例
            Position updatedPosition = new Position();
            updatedPosition.setPosId(posId);
            return updatedPosition;
        } else if (response.statusCode() == 404) {
            throw new RuntimeException("更新失败: 未找到职位 ID " + posId);
        } else {
            throw new RuntimeException("更新职位失败: " + response.body());
        }
    }

    // --- 4. 删除职位 (D) ---
    public void deletePosition(Integer posId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + posId))
                .DELETE()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("删除职位失败: " + response.body());
        }
    }
}