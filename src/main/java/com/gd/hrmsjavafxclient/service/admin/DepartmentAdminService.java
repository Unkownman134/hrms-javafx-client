package com.gd.hrmsjavafxclient.service.admin;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gd.hrmsjavafxclient.model.Department;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class DepartmentAdminService {
    private static final String BASE_URL = "http://localhost:8080/api/departments";
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<Department> getAllDepartments() throws Exception {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL)).GET().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<List<Department>>() {});
        }
        throw new RuntimeException("部门查询失败");
    }

    public Department createDepartment(Department d) throws Exception {
        String body = objectMapper.writeValueAsString(d);
        HttpRequest req = HttpRequest.newBuilder().uri(URI.create(BASE_URL)).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(body)).build();
        return objectMapper.readValue(httpClient.send(req, HttpResponse.BodyHandlers.ofString()).body(), Department.class);
    }

    public Department updateDepartment(Integer id, Department d) throws Exception {
        String body = objectMapper.writeValueAsString(d);
        HttpRequest req = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/" + id)).header("Content-Type", "application/json").PUT(HttpRequest.BodyPublishers.ofString(body)).build();
        httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        return d;
    }

    public void deleteDepartment(Integer id) throws Exception {
        HttpRequest req = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/" + id)).DELETE().build();
        httpClient.send(req, HttpResponse.BodyHandlers.ofString());
    }
}