package com.kousenit.ollama;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.OffsetDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.kousenit.ollama.OllamaRecords.*;

public class OllamaService {
    public static final String OLLAMA_BASE_URL = "http://localhost:11434";
    public static final String LLAMA3 = "llama3";
    public static final String LLAVA = "llava";

    private final Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .registerTypeAdapter(OffsetDateTime.class, new OffsetDateTimeDeserializer())
            .create();

    public OllamaModels listLocalModels() {
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("%s/api/tags".formatted(OLLAMA_BASE_URL)))
                    .header("Accept", "application/json")
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return gson.fromJson(response.body(), OllamaModels.class);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

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

    public void streaming(String model, String prompt) {
        String url = "http://localhost:11434/api/generate";
        String jsonRequestBody = """
                {
                    "model": "%s",
                    "prompt": "%s"
                }""".formatted(model, prompt);

        CompletableFuture<Void> future;
        try (var client = HttpClient.newHttpClient()) {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("Accept", "text/event-stream")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonRequestBody))
                    .build();

            future = client.sendAsync(request, HttpResponse.BodyHandlers.ofLines())
                    .thenAccept(response -> response.body().forEach(line -> {
                                if (!line.contains("\"done\":true")) {
                                    // Process each line of the response
                                    var ollamaStreamingResponse =
                                            gson.fromJson(line, OllamaStreamingResponse.class);
                                    System.out.print(ollamaStreamingResponse.response());
                                } else {
                                    // If "done" is true, the response is complete
                                    var ollamaCompletedResponse =
                                            gson.fromJson(line, OllamaCompletedResponse.class);
                                    System.out.println(ollamaCompletedResponse);
                                    System.out.println("\nResponse is complete");
                                    double speed =
                                            (double) ollamaCompletedResponse.evalCount() / ollamaCompletedResponse.evalDuration()
                                                   * 10e9;
                                    System.out.printf("Speed: %.2f tokens/sec%n", speed);
                                }
                            }));
        }

        // Optionally, wait for the future to complete if you want to block the main thread
        try {
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error processing streaming response: " + e.getMessage());
        }
    }
}
