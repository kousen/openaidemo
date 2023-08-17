package com.kousenit.stabilityai;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kousenit.openai.LowercaseEnumSerializer;
import com.kousenit.openai.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.stream.IntStream;

public class StabilityAI {
    private static final String BASE_URL = "https://api.stability.ai";
    private final Logger logger = LoggerFactory.getLogger(StabilityAI.class);

    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .registerTypeAdapter(Role.class, new LowercaseEnumSerializer())
            .create();

    private final HttpClient client = HttpClient.newHttpClient();
    private final String authHeader = "Bearer %s".formatted(System.getenv("STABILITY_API_KEY"));

    public Balance getBalance() {
        return makeGetRequest("/v1/user/balance", Balance.class);
    }

    public Engines getEngines() {
        Engine[] enginesArray = makeGetRequest("/v1/engines/list", Engine[].class);
        return new Engines(Arrays.asList(enginesArray));
    }

    public void generateImages(String prompt) {
        Payload payload = new Payload(7, "NONE", "photographic",
                1024, 1024, "K_DPM_2_ANCESTRAL", 1, 75,
                List.of(new TextPrompt(prompt, 1)));

        Artifacts response = makePostRequest(
                "/v1/generation/stable-diffusion-xl-1024-v1-0/text-to-image",
                        gson.toJson(payload), Artifacts.class);

        writeResponseToFile(response);
        saveImages(response.artifacts());
    }

    private <T> T makeGetRequest(String endpoint, Class<T> type) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Authorization", authHeader)
                .header("Content-Type", "application/json")
                .GET()
                .build();

        return sendRequest(request, type);
    }

    @SuppressWarnings("SameParameterValue")
    private <T> T makePostRequest(String endpoint, String json, Class<T> type) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Authorization", authHeader)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        return sendRequest(request, type);
    }

    private <T> T sendRequest(HttpRequest request, Class<T> type) {
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return gson.fromJson(response.body(), type);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeResponseToFile(Artifacts response) {
        try {
            Files.copy(
                    new ByteArrayInputStream(response.toString().getBytes()),
                    Path.of("response.json"),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void saveImages(List<Image> images) {
        IntStream.range(0, images.size()).forEach(i -> {
            Image image = images.get(i);
            try {
                byte[] imgBytes = Base64.getDecoder().decode(image.base64());
                Files.write(Paths.get("image" + i + ".png"), imgBytes);
                logger.info("Wrote image{} to image{}.png", i, i);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }
}