package com.gd.hrmsjavafxclient.service.admin;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gd.hrmsjavafxclient.model.Employee;
import com.gd.hrmsjavafxclient.model.Position;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

public class EmployeeAdminService {
    private static final String BASE_URL = "http://localhost:8080/api/employees";
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper;

    public EmployeeAdminService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public List<Employee> getAllEmployees() throws Exception {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL)).GET().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<List<Employee>>() {});
        }
        throw new RuntimeException("获取员工列表失败");
    }

    public Employee createEmployee(Employee employee) throws Exception {
        String body = objectMapper.writeValueAsString(employee);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200 || response.statusCode() == 201) {
            String respBody = response.body();
            if (respBody != null && respBody.trim().startsWith("{")) {
                return objectMapper.readValue(respBody, Employee.class);
            }
            return employee;
        } else {
            throw new RuntimeException("新增失败: " + response.body());
        }
    }

    public Employee updateEmployee(Integer empId, Employee employee) throws Exception {
        String body = objectMapper.writeValueAsString(employee);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + empId))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) return employee;
        throw new RuntimeException("更新失败: " + response.body());
    }

    public void deleteEmployee(Integer empId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/" + empId)).DELETE().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) throw new RuntimeException("删除失败");
    }

    public List<Map<String, Object>> getAllDepartments() throws Exception {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create("http://localhost:8080/api/departments")).GET().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return objectMapper.readValue(response.body(), new TypeReference<>() {});
    }

    public List<Map<String, Object>> getAllPositions() throws Exception {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create("http://localhost:8080/api/positions")).GET().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return objectMapper.readValue(response.body(), new TypeReference<>() {});
    }
}