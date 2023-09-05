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
import java.util.Arrays;
import java.util.Random;

public class DallE {
    private static final String URL = "https://api.openai.com/v1/images/generations";

    private final Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    private final HttpClient client = HttpClient.newHttpClient();

    public int getSingleImage(String prompt) {
        ImageRequest request = new ImageRequest(prompt, 1, "1024x1024");
        ImageResponse response = sendImagePrompt(request);
        Arrays.stream(response.data())
                .map(Image::url)
                .forEach(this::writeImageToFile);
        return response.data().length;
    }

    private void writeImageToFile(String url) {
        Random random = new Random();
        String fileName = "image%d.png".formatted(random.nextInt());
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();
        HttpResponse<InputStream> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
            Files.copy(response.body(), Paths.get("src/main/resources", fileName),
                    StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Wrote " + fileName + " to src/main/resources");
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public ImageResponse sendImagePrompt(ImageRequest imageRequest) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL))
                .header("Authorization",
                        "Bearer %s".formatted(System.getenv("OPENAI_API_KEY")))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(imageRequest)))
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