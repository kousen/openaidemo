package com.kousenit.openai.chat;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kousenit.openai.json.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

@SuppressWarnings("DuplicatedCode")
public class GroqChatGPT {
    private static final String CHAT_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String GROQ_API_KEY = System.getenv("GROQ_API_KEY");

    public static final String LLAMA3 = "llama3-70b-8192";
    public static final String MIXTRAL = "mixtral-8x7b-32768";
    public static final String GEMMA = "gemma-7b-it";

    public final static double DEFAULT_TEMPERATURE = 0.7;

    private final Logger logger = LoggerFactory.getLogger(GroqChatGPT.class);

    private final Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .registerTypeAdapter(Role.class, new LowercaseEnumSerializer())
            .create();

    private final HttpClient client = HttpClient.newHttpClient();

    public String getResponse(String prompt, String model) {
        return getResponse(prompt, model, DEFAULT_TEMPERATURE);
    }

    public String getResponse(String prompt, String model, double temperature) {
        ChatRequest chatRequest = createChatRequest(prompt, model, temperature);
        ChatResponse chatResponse = createChatResponse(chatRequest);
        return chatResponse.choices().getFirst().message().content();
    }

    public ChatRequest createChatRequest(String prompt, String model, double temperature) {
        return new ChatRequest(model,
                List.of(new Message(Role.USER, prompt)),
                temperature);
    }

    public String getResponseToMessages(String model, Message... messages) {
        List<Message> messageList = List.of(messages);
        ChatRequest chatRequest = new ChatRequest(model, messageList, DEFAULT_TEMPERATURE);
        long startTime = System.nanoTime();
        ChatResponse chatResponse = createChatResponse(chatRequest);
        long endTime = System.nanoTime();
        logger.info("Elapsed time: {} ms", (endTime - startTime) / 1_000_000);
        logger.info(chatResponse.usage().toString());
        return extractStringResponse(chatResponse);
    }

    public String extractStringResponse(ChatResponse chatResponse) {
        return chatResponse.choices().getFirst().message().content();
    }

    // Transmit the request to the OpenAI API and return the response
    public ChatResponse createChatResponse(ChatRequest chatRequest) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(CHAT_URL))
                .header("Authorization", "Bearer %s".formatted(GROQ_API_KEY))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(chatRequest)))
                .build();
        try {
            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new RuntimeException("Error: " + response.statusCode() + ": " + response.body());
            }
            return gson.fromJson(response.body(), ChatResponse.class);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}