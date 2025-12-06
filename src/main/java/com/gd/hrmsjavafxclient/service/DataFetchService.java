package com.gd.hrmsjavafxclient.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gd.hrmsjavafxclient.model.Employee;
import com.gd.hrmsjavafxclient.model.Position;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class DataFetchService {

    private static final String BASE_URL = "http://localhost:8080/api";
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 复用 UserService, EmployeeService, PositionController 暴露的 API

    public Employee getEmployeeById(Integer empId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/employees/" + empId))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), Employee.class);
        } else {
            System.err.println("查询员工失败，EmpID: " + empId + ", 状态码: " + response.statusCode());
            return null;
        }
    }

    public Position getPositionById(Integer posId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/positions/" + posId))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), Position.class);
        } else {
            System.err.println("查询职位失败，PosID: " + posId + ", 状态码: " + response.statusCode());
            return null;
        }
    }
}