package com.gd.hrmsjavafxclient.service.admin;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gd.hrmsjavafxclient.model.Position;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class PositionAdminService {
    private static final String BASE_URL = "http://localhost:8080/api/positions";
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<Position> getAllPositions() throws Exception {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL)).GET().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<List<Position>>() {});
        }
        throw new RuntimeException("查询失败");
    }

    public Position createPosition(Position p) throws Exception {
        String body = objectMapper.writeValueAsString(p);
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL)).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(body)).build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return objectMapper.readValue(response.body(), Position.class);
    }

    public Position updatePosition(Integer id, Position p) throws Exception {
        String body = objectMapper.writeValueAsString(p);
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/" + id)).header("Content-Type", "application/json").PUT(HttpRequest.BodyPublishers.ofString(body)).build();
        httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return p;
    }

    public void deletePosition(Integer id) throws Exception {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/" + id)).DELETE().build();
        httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
}