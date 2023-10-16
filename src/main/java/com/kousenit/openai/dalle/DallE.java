package com.kousenit.openai.dalle;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kousenit.utilities.FileUtils;
import com.kousenit.openai.json.Image;
import com.kousenit.openai.json.ImageRequest;
import com.kousenit.openai.json.ImageResponse;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class DallE {
    private static final String URL = "https://api.openai.com/v1/images/generations";

    private final Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    private final HttpClient client = HttpClient.newHttpClient();

    public long getImages(String prompt, int numberOfImages) {
        ImageRequest request = new ImageRequest(
                prompt, numberOfImages, "512x512", "b64_json");
        ImageResponse response = sendImagePrompt(request);
        return response.data().stream()
                .map(Image::b64Json)
                .filter(FileUtils::writeImageToFile)
                .count();
    }

    public ImageResponse sendImagePrompt(ImageRequest imageRequest) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(DallE.URL))
                .header("Authorization", "Bearer %s".formatted(System.getenv("OPENAI_API_KEY")))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(imageRequest)))
                .build();
        try {
            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Status: " + response.statusCode());
            return gson.fromJson(response.body(), ImageResponse.class);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Error sending image prompt", e);
        }
    }
}