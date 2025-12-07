package com.gd.hrmsjavafxclient.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MapperFeature; // 🌟 导入
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gd.hrmsjavafxclient.model.SalaryStandard;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

/**
 * 客户端 R8: 薪酬标准管理服务 (CRUD) - 还原为不带认证的版本
 */
public class SalaryStandardAdminService {

    // 🌟 修正 URL 路径，与后端 Controller @RequestMapping("/api/salary/standards") 保持一致
    private static final String BASE_URL = "http://localhost:8080/api/salary/standards";
    private final HttpClient httpClient = HttpClient.newHttpClient();

    // 🌟 核心修复点：配置 ObjectMapper 允许大小写不敏感的属性匹配
    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);

    // --- 1. 查询所有 (R) ---
    public List<SalaryStandard> getAllSalaryStandards() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<List<SalaryStandard>>() {});
        } else {
            // 保持和 R1/R9 一致的简单报错
            throw new RuntimeException("查询薪酬标准失败，状态码: " + response.statusCode() + " 错误详情: " + response.body());
        }
    }

    // --- 2. 创建薪酬标准 (C) ---
    public SalaryStandard createSalaryStandard(SalaryStandard newStandard) throws Exception {

        // ⚠️ 注意：创建时不应上传 stdId, totalAmount（TotalAmount 是客户端计算字段，stdId 是后端自增）
        // 为了简化，我们只创建用于传输的对象
        SalaryStandard createPayload = new SalaryStandard();
        createPayload.setStandardName(newStandard.getStandardName());
        createPayload.setBasicSalary(newStandard.getBasicSalary());      // 🌟 使用修正后的 basicSalary
        createPayload.setMealAllowance(newStandard.getMealAllowance());  // 🌟 使用修正后的 mealAllowance
        createPayload.setAllowances(newStandard.getAllowances());        // 🌟 使用修正后的 allowances

        String requestBody = objectMapper.writeValueAsString(createPayload);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 201) {
            // 返回创建成功的对象，包含后端生成的 StdID
            return objectMapper.readValue(response.body(), SalaryStandard.class);
        } else {
            throw new RuntimeException("创建薪酬标准失败，状态码: " + response.statusCode() + " 错误详情: " + response.body());
        }
    }

    // --- 3. 更新薪酬标准 (U) ---
    public SalaryStandard updateSalaryStandard(Integer standardId, SalaryStandard standardDetails) throws Exception {

        // ⚠️ 更新时，我们只发送需要更新的字段（名称和三项薪资）
        SalaryStandard updatePayload = new SalaryStandard();
        updatePayload.setStandardName(standardDetails.getStandardName());
        updatePayload.setBasicSalary(standardDetails.getBasicSalary());      // 🌟 使用修正后的 basicSalary
        updatePayload.setMealAllowance(standardDetails.getMealAllowance());  // 🌟 使用修正后的 mealAllowance
        updatePayload.setAllowances(standardDetails.getAllowances());        // 🌟 使用修正后的 allowances

        String requestBody = objectMapper.writeValueAsString(updatePayload);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + standardId))
                .header("Content-Type", "application/json")
                // 后端使用 PUT，我们保持一致
                .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            // 成功后，将 ID 设回并返回给 Controller 更新列表
            standardDetails.setStdId(standardId); // 🌟 使用修正后的 setStdId
            return standardDetails;
        } else if (response.statusCode() == 404) {
            throw new RuntimeException("更新失败: 未找到薪酬标准 ID " + standardId);
        } else {
            throw new RuntimeException("更新薪酬标准失败: " + response.body());
        }
    }

    // --- 4. 删除薪酬标准 (D) ---
    public void deleteSalaryStandard(Integer standardId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + standardId))
                .DELETE()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("删除薪酬标准失败，状态码: " + response.statusCode() + " 错误详情: " + response.body());
        }
    }
}