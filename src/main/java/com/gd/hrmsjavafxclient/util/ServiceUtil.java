package com.gd.hrmsjavafxclient.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

public final class ServiceUtil {

    private static final String BASE_URL = "http://localhost:8080/api";

    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    private static final ObjectMapper OBJECT_MAPPER;

    static {
        OBJECT_MAPPER = new ObjectMapper();
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
    }

    private ServiceUtil() {}


    public static <T> Optional<T> sendGet(String endpoint, String authToken, TypeReference<T> responseTypeRef)
            throws IOException, InterruptedException {

        String url = BASE_URL + endpoint;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + authToken)
                .GET()
                .build();

        System.out.println("--- API GET 请求 ---");
        System.out.println("URL: " + url);

        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            if (response.body() != null && !response.body().isBlank()) {
                return Optional.of(OBJECT_MAPPER.readValue(response.body(), responseTypeRef));
            }
            return Optional.empty();
        } else if (response.statusCode() == 404) {
            return Optional.empty();
        } else {
            throw new RuntimeException(String.format("API 请求失败 [%s]，状态码: %d，响应: %s",
                    url, response.statusCode(), response.body()));
        }
    }


    public static <T, R> Optional<R> sendRequest(String endpoint, String authToken, T body, String method, TypeReference<R> responseTypeRef)
            throws IOException, InterruptedException {

        String url = BASE_URL + endpoint;
        HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(url));

        builder.header("Authorization", "Bearer " + authToken)
                .header("Content-Type", "application/json");

        String upperMethod = method.toUpperCase();
        switch (upperMethod) {
            case "POST":
            case "PUT":
                String jsonBody = body != null ? OBJECT_MAPPER.writeValueAsString(body) : "";
                builder.method(upperMethod, HttpRequest.BodyPublishers.ofString(jsonBody));
                break;
            case "DELETE":
                builder.DELETE();
                break;
            default:
                throw new IllegalArgumentException("不支持的 HTTP 方法: " + method);
        }

        HttpRequest request = builder.build();

        System.out.println("--- API " + upperMethod + " 请求 ---");
        System.out.println("URL: " + url);

        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        int statusCode = response.statusCode();
        String responseBody = response.body();

        if (statusCode >= 200 && statusCode < 300) {
            if (responseBody != null && !responseBody.isBlank() && responseTypeRef != null) {
                try {
                    return Optional.of(OBJECT_MAPPER.readValue(responseBody, responseTypeRef));
                } catch (Exception e) {
                    if (responseBody.contains("成功") || responseBody.contains("success")) {
                        System.out.println("[DEBUG] 收到非 JSON 成功响应: " + responseBody);
                        return Optional.empty();
                    }
                    throw e;
                }
            }
            return Optional.empty();
        } else {
            throw new RuntimeException(String.format("API 请求失败 [%s %s]，状态码: %d，响应: %s",
                    upperMethod, url, statusCode, responseBody));
        }
    }
}