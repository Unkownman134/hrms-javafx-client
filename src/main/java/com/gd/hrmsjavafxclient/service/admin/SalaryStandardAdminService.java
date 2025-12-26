package com.gd.hrmsjavafxclient.service.admin;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gd.hrmsjavafxclient.model.SalaryStandard;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;


public class SalaryStandardAdminService {

    private static final String BASE_URL = "http://localhost:8080/api/salary/standards";
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);

    public List<SalaryStandard> getAllSalaryStandards() throws Exception {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL)).GET().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<List<SalaryStandard>>() {});
        } else {
            throw new RuntimeException("查询失败: " + response.statusCode());
        }
    }

    public SalaryStandard createSalaryStandard(SalaryStandard standard) throws Exception {
        String json = objectMapper.writeValueAsString(standard);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200 || response.statusCode() == 201) {
            String body = response.body();
            if (body != null && body.trim().startsWith("{")) {
                return objectMapper.readValue(body, SalaryStandard.class);
            } else {
                return standard;
            }
        } else {
            throw new RuntimeException("创建失败: " + response.body());
        }
    }

    public SalaryStandard updateSalaryStandard(Integer id, SalaryStandard standard) throws Exception {
        String json = objectMapper.writeValueAsString(standard);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + id))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return standard;
        } else {
            throw new RuntimeException("更新失败: " + response.body());
        }
    }

    public void deleteSalaryStandard(Integer id) throws Exception {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/" + id)).DELETE().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException("删除失败: " + response.body());
        }
    }
}