package com.gd.hrmsjavafxclient.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gd.hrmsjavafxclient.model.Position; // 假设会用到

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
 * 🌟 修正：让 sendRequest 正确识别所有 2xx 成功状态码 (200, 201, 204 等)。
 * 确保原有 sendGet 方法保持不变和可用性。
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
     * 禁用构造函数，确保这是一个静态工具类
     */
    private ServiceUtil() {}

    // --- 通用 GET 请求方法 (原版，保持不变，功能正常) ---

    /**
     * 发送 GET 请求并解析响应体。
     * 期望状态码：200 OK
     * @param endpoint API 端点路径
     * @param authToken 认证 Token
     * @param responseTypeRef 响应体的类型引用
     * @return 包含解析对象的 Optional
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
            throw new RuntimeException(String.format("API 请求失败 [%s]，状态码: %d，响应体: %s",
                    url, response.statusCode(), response.body()));
        }
    }

    // --- 通用 POST / PUT / DELETE 请求方法 (兼容 2xx 状态码) ---

    /**
     * 发送 POST/PUT/DELETE 请求。
     * 期望状态码：2xx 范围 (200 OK, 201 Created, 204 No Content)
     * @param endpoint API 端点路径
     * @param authToken 认证 Token
     * @param body 请求体对象 (POST/PUT 有效，DELETE 可传 null)
     * @param method HTTP 方法 ("POST", "PUT", "DELETE")
     * @param responseTypeRef 响应体的类型引用 (如果期望有返回值)
     * @return 包含解析对象的 Optional
     * @throws IOException IO 错误
     * @throws InterruptedException 线程中断
     * @throws RuntimeException API 请求状态码非 2xx
     */
    public static <T, R> Optional<R> sendRequest(String endpoint, String authToken, T body, String method, TypeReference<R> responseTypeRef)
            throws IOException, InterruptedException {

        String url = BASE_URL + endpoint;
        HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(url));

        // 设置 Header
        builder.header("Authorization", "Bearer " + authToken)
                .header("Content-Type", "application/json");

        // 处理请求体和方法
        if ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method)) {
            String jsonBody = body != null ? OBJECT_MAPPER.writeValueAsString(body) : "";
            builder.method(method, HttpRequest.BodyPublishers.ofString(jsonBody));
        } else if ("DELETE".equalsIgnoreCase(method)) {
            builder.DELETE();
        } else {
            // 默认方法处理 (防止传入错误的 Method)
            throw new IllegalArgumentException("不支持的 HTTP 方法: " + method);
        }

        HttpRequest request = builder.build();

        System.out.println("--- API 请求 ---");
        System.out.println(method + " URL: " + url);

        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        // 🌟 核心修正：接受所有 2xx 状态码 (200 <= status < 300)
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            // POST 期望 201 Created，PUT 期望 200 OK，DELETE 期望 204 No Content
            if (response.body() != null && !response.body().isEmpty() && responseTypeRef.getType() != Void.class) {
                // 成功且有响应体，解析并返回
                return Optional.of(OBJECT_MAPPER.readValue(response.body(), responseTypeRef));
            } else {
                // 成功但无响应体 (如 204, 或客户端不需要响应体)
                return Optional.empty();
            }
        } else {
            // 抛出异常，附带详细信息
            throw new RuntimeException(String.format("API 请求失败 [%s]，状态码: %d，响应体: %s",
                    url, response.statusCode(), response.body()));
        }
    }
}