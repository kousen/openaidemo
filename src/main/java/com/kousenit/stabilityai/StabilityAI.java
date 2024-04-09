package com.kousenit.stabilityai;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kousenit.stabilityai.json.*;
import com.kousenit.utilities.FileUtils;
import org.apache.hc.client5.http.ClientProtocolException;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.StatusLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public class StabilityAI {
    private static final String BASE_URL = "https://api.stability.ai";
    private static final String SDXL_ENGINE = "stable-diffusion-xl-1024-v1-0";
    private static final String KEY = System.getenv("STABILITY_API_KEY");

    private final Logger logger = LoggerFactory.getLogger(StabilityAI.class);

    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    private final HttpClient client = HttpClient.newHttpClient();
    private final String authHeader = "Bearer %s".formatted(KEY);

    public Balance getBalance() {
        return makeGetRequest("/v1/user/balance", Balance.class);
    }

    public Engines getEngines() {
        Engine[] enginesArray = makeGetRequest("/v1/engines/list", Engine[].class);
        return new Engines(Arrays.asList(enginesArray));
    }

    public void generateImages(String prompt, int numberOfImages) {
        Payload payload = new Payload(
                7, "NONE", "photographic",
                1024, 1024, numberOfImages, 40,
                List.of(new TextPrompt(prompt, 0.5)));

        Artifacts response = makePostRequest(
                "/v1/generation/%s/text-to-image".formatted(SDXL_ENGINE),
                gson.toJson(payload), Artifacts.class);

        long count = response.artifacts()
                .stream()
                .filter(image -> !image.finishReason()
                        .equals(FinishReason.ERROR))
                .map(Image::base64)
                .filter(FileUtils::writeImageToFile)
                .count();
        logger.info("Wrote {} images to src/main/resources/images", count);
    }

    private <T> T makeGetRequest(String endpoint, Class<T> type) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Authorization", authHeader)
                .header("Accept", "application/json")
                .GET()
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return gson.fromJson(response.body(), type);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("SameParameterValue")
    private <T> T makePostRequest(String endpoint, String json, Class<T> type) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Authorization", authHeader)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        try {
            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());
            return gson.fromJson(response.body(), type);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    // New version for Stability AI
    public void requestStableImage(String prompt) throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost("https://api.stability.ai/v2beta/stable-image/generate/core");
            post.setHeader("Authorization", "Bearer %s".formatted(KEY));
            post.setHeader("Accept", "image/*");

            HttpEntity entity = MultipartEntityBuilder.create()
                    .addTextBody("prompt", prompt, ContentType.TEXT_PLAIN)
                    .addTextBody("output_format", "png", ContentType.TEXT_PLAIN)
                    .build();

            post.setEntity(entity);

            client.execute(post, response -> {
                if (response.getCode() != 200) {
                    throw new ClientProtocolException(new StatusLine(response).toString());
                }
                System.out.println(post + " -> " + new StatusLine(response));
                String fileName = "stabilityai_%s_%s.png".formatted(
                        LocalDate.now(),
                        prompt.replaceAll("\\s+", "_").substring(0, 20));
                Path filePath = Paths.get("src/main/resources/images/stablediffusion", fileName);
                Files.write(filePath, EntityUtils.toByteArray(response.getEntity()));
                System.out.println(filePath.toAbsolutePath() + " downloaded successfully.");
                return null;
            });
        }

    }
}