package com.gd.hrmsjavafxclient.service.admin;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gd.hrmsjavafxclient.model.Position;
import com.gd.hrmsjavafxclient.model.SalaryStandard;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class PositionAdminService {
    private static final String BASE_URL = "http://localhost:8080/api/positions";
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<Position> getAllPositions() throws Exception {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL)).GET().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<List<Position>>() {});
        }
        throw new RuntimeException("查询职位失败");
    }

    public List<SalaryStandard> getAllSalaryStandards() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/salary/standards"))
                .GET().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<List<SalaryStandard>>() {});
        }
        throw new RuntimeException("获取薪资标准失败");
    }

    public Position createPosition(Position p) throws Exception {
        String bodyContent = objectMapper.writeValueAsString(p);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(bodyContent))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200 || response.statusCode() == 201) {
            String respBody = response.body();
            if (respBody != null && respBody.trim().startsWith("{")) {
                return objectMapper.readValue(respBody, Position.class);
            }
            return p;
        } else {
            throw new RuntimeException("新增职位失败: " + response.body());
        }
    }

    public Position updatePosition(Integer id, Position p) throws Exception {
        String bodyContent = objectMapper.writeValueAsString(p);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + id))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(bodyContent))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return p;
        } else {
            throw new RuntimeException("修改职位失败: " + response.body());
        }
    }

    public void deletePosition(Integer id) throws Exception {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/" + id)).DELETE().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException("删除失败: " + response.body());
        }
    }
}