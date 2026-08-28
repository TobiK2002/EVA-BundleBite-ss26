package rest.client;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class ApiClient {

    private static final String DEFAULT_BASE_URL =
            "http://localhost:8080/api";

    private final String baseUrl;

    private final HttpClient httpClient =
            HttpClient.newHttpClient();

    private final ObjectMapper objectMapper =
            new ObjectMapper();

    public ApiClient() {
        this(DEFAULT_BASE_URL);
    }

    public ApiClient(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public <T> T get(String path, Class<T> responseType) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        validateResponse(response);

        return objectMapper.readValue(response.body(), responseType);
    }

    public <T> List<T> getList(String path, TypeReference<List<T>> responseType) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        validateResponse(response);

        return objectMapper.readValue(response.body(), responseType);
    }

    public <T> T post(String path, Object body, Class<T> responseType) throws IOException, InterruptedException {
        String json = objectMapper.writeValueAsString(body);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        validateResponse(response);

        return objectMapper.readValue(response.body(), responseType);
    }

    public void put(String path, Object body) throws IOException, InterruptedException {
        String json = objectMapper.writeValueAsString(body);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        validateResponse(response);
    }

    public void delete(String path) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .DELETE()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        validateResponse(response);
    }

    public void validateResponse(HttpResponse<String> response) {
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException("HTTP " + response.statusCode() + ": " + response.body());
        }
    }
}
