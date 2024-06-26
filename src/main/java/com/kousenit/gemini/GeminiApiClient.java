package com.kousenit.gemini;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static com.kousenit.gemini.GeminiRecords.*;

public class GeminiApiClient {

    private static final String API_KEY = System.getenv("GOOGLEAI_API_KEY");
    private static final String CREATE_ENDPOINT =
            "https://generativelanguage.googleapis.com/v1beta/cachedContents?key=" + API_KEY;
    private static final String LIST_ENDPOINT =
            "https://generativelanguage.googleapis.com/v1beta/cachedContents";
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    public static CachedContent createCachedContent(CachedContent cachedContent) throws Exception {
        String requestBody = gson.toJson(cachedContent);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(CREATE_ENDPOINT))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return gson.fromJson(response.body(), CachedContent.class);
            } else {
                throw new RuntimeException("Failed to create CachedContent: " + response.body());
            }
        }
    }

    public static List<CachedContent> listCachedContents(Integer pageSize, String pageToken) throws Exception {
        StringBuilder urlBuilder = new StringBuilder(LIST_ENDPOINT);
        urlBuilder.append("?key=").append(API_KEY);
        if (pageSize != null) {
            urlBuilder.append("&pageSize=").append(pageSize);
        }
        if (pageToken != null) {
            urlBuilder.append("&pageToken=").append(pageToken);
        }

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(urlBuilder.toString()))
                .header("Content-Type", "application/json")
                .GET()
                .build();

        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonObject jsonObject = gson.fromJson(response.body(), JsonObject.class);
                Type listType = new TypeToken<List<CachedContent>>(){}.getType();
                return gson.fromJson(jsonObject.get("cachedContents"), listType);
            } else {
                throw new RuntimeException("Failed to list CachedContents: " + response.body());
            }
        }
    }

    public static GenerateContentResponse generateContent(
            String model, GenerateContentRequest request) {
        String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/" +
                          model + ":generateContent?key=" + API_KEY;
        String requestBody = gson.toJson(request);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpResponse<String> response =
                    client.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return gson.fromJson(response.body(), GenerateContentResponse.class);
            } else {
                throw new RuntimeException("Failed to generate content: " + response.body());
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}