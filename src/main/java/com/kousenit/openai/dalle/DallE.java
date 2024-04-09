package com.kousenit.openai.dalle;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kousenit.utilities.FileUtils;
import com.kousenit.openai.json.Image;
import com.kousenit.openai.json.ImageRequest;
import com.kousenit.openai.json.ImageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class DallE {
    private static final String URL = "https://api.openai.com/v1/images/generations";
    public static final String DALL_E_3 = "dall-e-3";
    public static final String DALL_E_2 = "dall-e-2";

    private final Logger logger = LoggerFactory.getLogger(DallE.class);

    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    public long getImages(String model, String prompt, int numberOfImages) {
        ImageRequest request = new ImageRequest(
                model, prompt, numberOfImages, "standard", "1024x1024", "b64_json");
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
        logger.debug("URI: {}", request.uri());
        logger.debug("Headers: {}", request.headers());
        logger.debug("Body: {}", gson.toJson(imageRequest));
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());
            logger.debug("Status: {}", response.statusCode());
            logger.debug("Headers: {}", response.headers());
            logger.debug("Request: {}", response.request());
            return gson.fromJson(response.body(), ImageResponse.class);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Error sending image prompt", e);
        }
    }
}