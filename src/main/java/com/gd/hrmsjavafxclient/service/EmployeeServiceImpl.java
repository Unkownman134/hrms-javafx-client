package com.gd.hrmsjavafxclient.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gd.hrmsjavafxclient.model.AttendanceRecord;
import com.gd.hrmsjavafxclient.model.SalaryRecord;
import com.gd.hrmsjavafxclient.model.ApprovalRequest;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

/**
 * 员工自助服务API实现 (同步模式)
 */
public class EmployeeServiceImpl implements EmployeeService {

    private static final String BASE_URL = "http://localhost:8080/api"; // 假设API基础URL
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper;

    public EmployeeServiceImpl() {
        // 配置 ObjectMapper 以支持 LocalDate, LocalTime 等 Java 8 时间类型
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    // --- 辅助方法：发送 GET 请求并解析 List<T> (同步) ---
    private <T> List<T> sendGetRequestAndParseList(String path, String authToken, TypeReference<List<T>> typeRef) throws Exception {
        String url = BASE_URL + path;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + authToken)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), typeRef);
        } else if (response.statusCode() == 401 || response.statusCode() == 403) {
            throw new RuntimeException("认证失败或无权限。请重新登录。");
        } else if (response.statusCode() == 404) {
            // 某些情况下 404 可能表示记录为空，这里我们倾向于抛出通用错误
            throw new RuntimeException("API 接口未找到或数据不存在。");
        } else {
            throw new RuntimeException("API 调用失败，状态码: " + response.statusCode() + ", Body: " + response.body());
        }
    }


    // --- 1. 查询考勤记录 (R5) ---
    @Override
    public List<AttendanceRecord> getAttendanceRecords(int empId, String yearMonth, String authToken) throws Exception {
        // API: GET /api/attendance/{EmpID}?month=YYYY-MM
        String path = String.format("/attendance/%d?month=%s", empId, yearMonth);
        return sendGetRequestAndParseList(path, authToken, new TypeReference<List<AttendanceRecord>>() {});
    }

    // --- 2. 查询工资条记录 (R6) ---
    @Override
    public List<SalaryRecord> getSalaryRecords(int empId, int year, String authToken) throws Exception {
        // API: GET /api/salary/history/{empId}?year=YYYY
        String path = String.format("/salary/history/%d?year=%d", empId, year);
        return sendGetRequestAndParseList(path, authToken, new TypeReference<List<SalaryRecord>>() {});
    }

    // --- 3. 提交申请 (R7) ---
    @Override
    public boolean submitApplication(ApprovalRequest request, String authToken) throws Exception {
        String path = "/approval-requests";

        String jsonBody = objectMapper.writeValueAsString(request);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + authToken)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        // 期望状态码 200 或 201
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return true;
        } else {
            throw new RuntimeException("提交申请失败，状态码: " + response.statusCode() + ", Body: " + response.body());
        }
    }
}