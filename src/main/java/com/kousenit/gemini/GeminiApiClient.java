package com.kousenit.gemini;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.kousenit.gemini.GeminiRecords.*;

public class GeminiApiClient {
    private static final String API_KEY = System.getenv("GOOGLEAI_API_KEY");
    private static final String BASE_URL = "https://generativelanguage.googleapis.com/v1beta";
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public ApiResult<CachedContent> createCachedContent(CachedContent cachedContent) {
        String endpoint = "%s/cachedContents?key=%s".formatted(BASE_URL, encodeValue(API_KEY));
        return sendRequest(endpoint, "POST", cachedContent, CachedContent.class);
    }

    public ApiResult<List<CachedContent>> listCachedContents(Integer pageSize, String pageToken) {
        var endpoint = "%s/cachedContents?key=%s%s%s".formatted(
                BASE_URL,
                encodeValue(API_KEY),
                pageSize != null ? "&pageSize=" + encodeValue(pageSize.toString()) : "",
                pageToken != null ? "&pageToken=" + encodeValue(pageToken) : ""
        );

        return switch (sendRequest(endpoint, "GET", null, JsonObject.class)) {
            case ApiResult.Success(var jsonObject) -> {
                if (jsonObject.has("cachedContents")) {
                    Type listType = new TypeToken<List<CachedContent>>(){}.getType();
                    List<CachedContent> contentList = gson.fromJson(jsonObject.get("cachedContents"), listType);
                    yield new ApiResult.Success<>(contentList);
                } else {
                    yield new ApiResult.Success<>(List.of()); // Return an empty list if no cached contents
                }
            }
            case ApiResult.Failure(var error) -> new ApiResult.Failure<>(error);
        };
    }
    public ApiResult<GenerateContentResponse> generateContent(String model, GenerateContentRequest request) {
        String endpoint = "%s/models/%s:generateContent?key=%s".formatted(BASE_URL, encodeValue(model), encodeValue(API_KEY));
        return sendRequest(endpoint, "POST", request, GenerateContentResponse.class);
    }

    private <T, R> ApiResult<R> sendRequest(String endpoint, String method, T requestBody, Class<R> responseClass) {
        try {
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json");

            requestBuilder = switch (method) {
                case "POST" -> requestBuilder.POST(HttpRequest.BodyPublishers.ofString(gson.toJson(requestBody)));
                case "GET" -> requestBuilder.GET();
                default -> throw new IllegalArgumentException("Unsupported HTTP method: " + method);
            };

            try (var httpClient = HttpClient.newHttpClient()) {
                HttpResponse<String> response = httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
                return handleResponse(response, responseClass);
            }
        } catch (IOException | InterruptedException e) {
            return new ApiResult.Failure<>("Request failed: " + e.getMessage());
        }
    }

    @SuppressWarnings("SwitchStatementWithTooFewBranches")
    private <R> ApiResult<R> handleResponse(HttpResponse<String> response, Class<R> responseClass) {
        return switch (response.statusCode()) {
            case HttpURLConnection.HTTP_OK -> new ApiResult.Success<>(gson.fromJson(response.body(), responseClass));
            default -> new ApiResult.Failure<>("Request failed with status %d: %s".formatted(response.statusCode(), response.body()));
        };
    }

    private String encodeValue(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}