package com.kousenit.openai;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class DallE {
    private static final String URL = "https://api.openai.com/v1/images/generations";

    private final Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    private final HttpClient client = HttpClient.newHttpClient();

    public String getResponse(String prompt) {
        ImagePrompt request = createImageRequest(prompt, 1, "1024x1024");
        ImageResponse response = sendImagePrompt(request);
        String url = response.data()[0].url();
        writeImageToFile(url);
        return url;
    }

    private void writeImageToFile(String url) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();
        HttpResponse<InputStream> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
            Files.copy(response.body(), Paths.get("src/main/resources/image.png"),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public ImagePrompt createImageRequest(String prompt, int n, String size) {
        return new ImagePrompt(prompt, n, size);
    }

    public ImageResponse sendImagePrompt(ImagePrompt imagePrompt) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL))
                .header("Authorization",
                        "Bearer %s".formatted(System.getenv("OPENAI_API_KEY")))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(imagePrompt)))
                .build();
        try {
            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println(response.statusCode() + ": " + response.body());
            return gson.fromJson(response.body(), ImageResponse.class);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}