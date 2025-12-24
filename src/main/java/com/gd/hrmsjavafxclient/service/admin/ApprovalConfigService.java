package com.gd.hrmsjavafxclient.service.admin;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gd.hrmsjavafxclient.model.ApprovalConfig;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class ApprovalConfigService {
    private static final String BASE_URL = "http://localhost:8080/api/approval-configs";
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<ApprovalConfig> getAllConfigs() throws Exception {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL)).GET().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<List<ApprovalConfig>>() {});
        }
        throw new RuntimeException("审批配置查询失败");
    }

    public void createConfig(ApprovalConfig config) throws Exception {
        // ✨ 新增逻辑：不发送 ID
        Map<String, Object> payload = new HashMap<>();
        payload.put("processType", config.getProcessType());
        payload.put("deptId", config.getDeptId());
        payload.put("approverPositionId", config.getApproverPositionId());

        String body = objectMapper.writeValueAsString(payload);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200 && response.statusCode() != 201) {
            throw new RuntimeException("新增配置失败: " + response.body());
        }
    }

    public void updateConfig(Integer id, ApprovalConfig config) throws Exception {
        // ✨ 更新逻辑：发送完整对象
        String body = objectMapper.writeValueAsString(config);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + id))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> response = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException("更新配置失败: " + response.body());
        }
    }

    public void deleteConfig(Integer id) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + id))
                .DELETE()
                .build();
        HttpResponse<String> response = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException("删除配置失败");
        }
    }
}