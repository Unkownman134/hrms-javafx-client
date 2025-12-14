package com.gd.hrmsjavafxclient.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule; // 🌟 导入 JavaTimeModule

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;

/**
 * 客户端通用服务工具类 (HttpClient, ObjectMapper 配置, 通用请求方法)
 * 封装了与后端 API 进行通信的底层逻辑。
 */
public final class ServiceUtil {

    // 假设API基础URL，通常应该放在配置文件中，这里先硬编码
    private static final String BASE_URL = "http://localhost:8080/api";

    // HTTP 客户端实例
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    // JSON 序列化/反序列化工具
    private static final ObjectMapper OBJECT_MAPPER;

    // 静态初始化块，配置 ObjectMapper
    static {
        OBJECT_MAPPER = new ObjectMapper();
        // 注册 JavaTimeModule 以正确处理 LocalDate, LocalTime 等 Java 8 时间类型
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
    }

    /**
     * 禁用构造函数，确保这是一个静态工具类。
     */
    private ServiceUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // --- 核心通用方法 ---

    /**
     * 【通用 GET 请求】发送 GET 请求并解析返回的 List<T>。
     * * @param path API 路径 (例如: "/attendance/1001?yearMonth=2025-11")
     * @param authToken 用户的认证Token
     * @param typeRef 用于反序列化 List<T> 的 TypeReference
     * @param <T> 列表中元素的类型
     * @return 解析后的对象列表
     * @throws Exception 如果网络请求失败或响应状态码非 200
     */
    public static <T> List<T> sendGetRequestAndParseList(
            String path,
            String authToken,
            TypeReference<List<T>> typeRef) throws Exception {

        String url = BASE_URL + path;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + authToken)
                .GET()
                .build();

        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            // 解析 JSON 列表
            return OBJECT_MAPPER.readValue(response.body(), typeRef);
        } else {
            // 抛出带有状态码和响应体的异常
            throw new RuntimeException(String.format("API 请求失败 [%s]，状态码: %d，响应体: %s",
                    path, response.statusCode(), response.body()));
        }
    }

    /**
     * 【通用 POST/PUT 请求】发送 POST/PUT 请求并处理响应，通常用于创建或更新。
     * * @param path API 路径 (例如: "/approval-requests")
     * @param authToken 用户的认证Token
     * @param requestBody 待发送的请求对象 (会被序列化成 JSON)
     * @param method HTTP 方法 ("POST" 或 "PUT")
     * @param responseTypeRef 期望返回的对象类型 (使用 TypeReference)
     * @param <T> 请求体的类型
     * @param <R> 期望返回的响应对象的类型
     * @return 解析后的响应对象 (Optional.empty() 表示成功但无体返回)
     * @throws Exception 如果网络请求失败或响应状态码不在 2xx 范围内
     */
    public static <T, R> Optional<R> sendRequest(
            String path,
            String authToken,
            T requestBody,
            String method,
            TypeReference<R> responseTypeRef) throws Exception {

        String url = BASE_URL + path;
        String jsonBody = OBJECT_MAPPER.writeValueAsString(requestBody);

        // 构建 HttpRequest.BodyPublisher
        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofString(jsonBody);

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + authToken);

        // 根据方法设置请求类型
        switch (method.toUpperCase()) {
            case "POST":
                builder.POST(bodyPublisher);
                break;
            case "PUT":
                builder.PUT(bodyPublisher);
                break;
            default:
                throw new IllegalArgumentException("不支持的 HTTP 方法: " + method);
        }

        HttpResponse<String> response = HTTP_CLIENT.send(builder.build(), HttpResponse.BodyHandlers.ofString());

        // 期望状态码 200 或 201
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            if (response.body() != null && !response.body().isEmpty()) {
                // 如果有响应体，解析并返回
                return Optional.of(OBJECT_MAPPER.readValue(response.body(), responseTypeRef));
            } else {
                // 成功但无响应体 (例如 POST 返回 201 Created 但无内容)
                return Optional.empty();
            }
        } else {
            // 抛出异常
            throw new RuntimeException(String.format("API 请求失败 [%s]，状态码: %d，响应体: %s",
                    path, response.statusCode(), response.body()));
        }
    }

    /**
     * 发送 GET 请求，并解析响应体。
     * @param <T> 预期的响应类型。
     * @param endpoint API 子路径 (例如: "/employees")
     * @param authToken 认证 Token
     * @param responseTypeRef 用于反序列化 List 或复杂对象的 TypeReference
     * @return 包含解析后的对象的 Optional，如果请求失败或无内容则返回 empty
     * @throws IOException IO 错误
     * @throws InterruptedException 线程中断
     * @throws RuntimeException API 请求状态码非 2xx
     */
    public static <T> Optional<T> sendGet(String endpoint, String authToken, TypeReference<T> responseTypeRef)
            throws IOException, InterruptedException {

        String url = BASE_URL + endpoint;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + authToken)
                .GET()
                .build();

        System.out.println("--- API 请求 ---");
        System.out.println("GET URL: " + url);

        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        // 期望状态码 200
        if (response.statusCode() == 200) {
            if (response.body() != null && !response.body().isEmpty()) {
                // 解析并返回
                return Optional.of(OBJECT_MAPPER.readValue(response.body(), responseTypeRef));
            } else {
                // 成功但无响应体
                return Optional.empty();
            }
        } else if (response.statusCode() == 404) {
            // 404，找不到资源，返回空
            return Optional.empty();
        } else {
            // 抛出异常
            throw new RuntimeException(String.format("API 请求失败 [%s]，状态码: %d，错误信息: %s",
                    url, response.statusCode(), response.body() != null ? response.body() : "未知错误"));
        }
    }
}