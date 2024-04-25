package com.kousenit.anthropic;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kousenit.anthropic.json.ChatRequest;
import com.kousenit.anthropic.json.ChatResponse;
import com.kousenit.anthropic.json.GsonLocalDateAdapter;
import com.kousenit.openai.chat.LowercaseEnumSerializer;
import com.kousenit.openai.chat.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;

public class ClaudeChat {
    private static final String CHAT_URL = "https://api.anthropic.com/v1/complete";
    private static final String ANTHROPIC_API_KEY = System.getenv("ANTHROPIC_API_KEY");
    public final static String CLAUDE_2 = "claude-2";
    private static final double DEFAULT_TEMPERATURE = 0.7;

    private final Logger logger = LoggerFactory.getLogger(ClaudeChat.class);

    private final Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .registerTypeAdapter(Role.class, new LowercaseEnumSerializer())
            .registerTypeAdapter(LocalDate.class, new GsonLocalDateAdapter())
            .create();

    private final HttpClient client = HttpClient.newHttpClient();

    public String getResponse(String prompt) {
        ChatRequest chatRequest = new ChatRequest(CLAUDE_2, formatPrompt(prompt),
                256, DEFAULT_TEMPERATURE);
        logger.debug("Request: {}", chatRequest);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(CHAT_URL))
                .header("x-api-key", ANTHROPIC_API_KEY)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("anthropic-version", "2023-06-01")
                .POST(HttpRequest.BodyPublishers.ofString(
                        gson.toJson(chatRequest)))
                .build();
        try {
            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new RuntimeException("Error: " + response.statusCode() + ": " + response.body());
            }
            ChatResponse chatResponse = gson.fromJson(response.body(), ChatResponse.class);
            logger.debug("Response: {}", chatResponse);
            return chatResponse.completion();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private String formatPrompt(String prompt) {
        return "\n\nHuman: %s\n\nAssistant:".formatted(prompt);
    }

}