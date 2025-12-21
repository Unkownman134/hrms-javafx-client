package com.gd.hrmsjavafxclient.service.admin;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gd.hrmsjavafxclient.model.Employee;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

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
        throw new RuntimeException("获取员工列表失败: " + response.statusCode());
    }

    public Employee createEmployee(Employee employee) throws Exception {
        String body = objectMapper.writeValueAsString(employee);
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL)).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(body)).build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return objectMapper.readValue(response.body(), Employee.class);
    }

    public Employee updateEmployee(Integer empId, Employee employee) throws Exception {
        String body = objectMapper.writeValueAsString(employee);
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/" + empId)).header("Content-Type", "application/json").PUT(HttpRequest.BodyPublishers.ofString(body)).build();
        httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return employee;
    }

    public void deleteEmployee(Integer empId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/" + empId)).DELETE().build();
        httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
}