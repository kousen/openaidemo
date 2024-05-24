package com.kousenit.ollama;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static com.kousenit.ollama.OllamaRecords.*;

public class OllamaService {
    public static final String OLLAMA_BASE_URL = "http://localhost:11434";
    public static final String LLAMA3 = "llama3";
    public static final String LLAVA = "llava";

    private final Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    public OllamaResponse generate(OllamaRequest request) {
        switch (request) {
            case OllamaTextRequest textRequest ->
                    System.out.printf("Generating text response from %s...%n", textRequest.model());
            case OllamaVisionRequest visionRequest ->
                    System.out.printf("Generating vision response from %s...%n", visionRequest.model());
        }
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create("%s/api/generate".formatted(OLLAMA_BASE_URL)))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(request)))
                    .build();
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            return gson.fromJson(response.body(), OllamaResponse.class);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public String generate(String model, String prompt) {
        return generate(new OllamaTextRequest(model, prompt, false)).response();

//        String request = """
//                {
//                    "model": "%s",
//                    "prompt": "%s",
//                    "stream": false
//                }
//                """.formatted(model, prompt);
//        try (HttpClient client = HttpClient.newHttpClient()) {
//            HttpRequest httpRequest = HttpRequest.newBuilder()
//                    .uri(URI.create("%s/api/generate".formatted(OLLAMA_BASE_URL)))
//                    .header("Content-Type", "application/json")
//                    .header("Accept", "application/json")
//                    .POST(HttpRequest.BodyPublishers.ofString(request))
//                    .build();
//            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
//            return response.body();
//        } catch (IOException | InterruptedException e) {
//            throw new RuntimeException(e);
//        }
    }
}
