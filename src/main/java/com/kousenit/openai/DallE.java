package com.kousenit.openai;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kousenit.openai.json.Image;
import com.kousenit.openai.json.ImageRequest;
import com.kousenit.openai.json.ImageResponse;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

public class DallE {
    private static final String URL = "https://api.openai.com/v1/images/generations";
    private int counter = 1;

    private final Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    private final HttpClient client = HttpClient.newHttpClient();

    public int getImages(String prompt, int numberOfImages) {
        ImageRequest request = new ImageRequest(
                prompt, numberOfImages, "512x512", "b64_json");
        ImageResponse response = sendImagePrompt(request);
        return (int) response.data().stream()
                .map(Image::b64Json)
                .filter(this::writeImageToFile)
                .count();
    }

    public ImageResponse sendImagePrompt(ImageRequest imageRequest) {
        HttpRequest request = createRequest(gson.toJson(imageRequest));
        try {
            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Status: " + response.statusCode());
            return gson.fromJson(response.body(), ImageResponse.class);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Error sending image prompt", e);
        }
    }

    private boolean writeImageToFile(String imageData) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String fileName = String.format("image_%s_%d.png", timestamp, counter++);
        Path directory = Paths.get("src/main/resources");
        Path filePath = directory.resolve(fileName);
        try {
            Files.createDirectories(directory);
            byte[] bytes = Base64.getDecoder().decode(imageData);
            Files.write(filePath, bytes, StandardOpenOption.CREATE_NEW);
            System.out.printf("Saved %s to src/main/resources%n", fileName);
            return true;
        } catch (IOException e) {
            throw new RuntimeException("Error writing image to file", e);
        }
    }

    private HttpRequest createRequest(String json) {
        return HttpRequest.newBuilder()
                .uri(URI.create(DallE.URL))
                .header("Authorization", "Bearer %s".formatted(System.getenv("OPENAI_API_KEY")))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
    }
}