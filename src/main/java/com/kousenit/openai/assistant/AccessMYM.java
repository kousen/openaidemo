package com.kousenit.openai.assistant;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class AccessMYM {
    private static final String MYM_ASSISTANT_ID = "asst_7ttDTA3qoaaDMLeo387TPWLM";
    private static final String ASSISTANT_URL = "https://api.openai.com/v1/assistants";

    private final HttpClient client = HttpClient.newHttpClient();

    private final Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    public String listAssistants() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ASSISTANT_URL))
                .header("Content-Type", "application/json")
                .header("Authorization",
                        "Bearer %s".formatted(System.getenv("OPENAI_API_KEY")))
                .header("OpenAI-Beta", "assistants=v1")
                .build();
        try {
            return client.send(request, HttpResponse.BodyHandlers.ofString()).body();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Assistant retrieveAssistant() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("%s/%s".formatted(ASSISTANT_URL, MYM_ASSISTANT_ID)))
                .header("Content-Type", "application/json")
                .header("Authorization",
                        "Bearer %s".formatted(System.getenv("OPENAI_API_KEY")))
                .header("OpenAI-Beta", "assistants=v1")
                .build();
        try {
            String response = client.send(request, HttpResponse.BodyHandlers.ofString()).body();
            return gson.fromJson(response, Assistant.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
