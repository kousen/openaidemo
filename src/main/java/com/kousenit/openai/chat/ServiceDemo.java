package com.kousenit.openai.chat;


import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kousenit.openai.json.ChatRequest;
import com.kousenit.openai.json.ChatResponse;
import com.kousenit.openai.json.Message;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class ServiceDemo {
    public static void main(String[] args) throws IOException, InterruptedException {
        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .registerTypeAdapter(Role.class, new LowercaseEnumSerializer())
                .create();

        ChatRequest chatRequest = new ChatRequest("gpt-3.5-turbo",
                List.of(new Message(Role.USER, "Say this is a test!")),
                0.7);
        System.out.println(chatRequest);
        System.out.println(gson.toJson(chatRequest));

        HttpResponse<String> response;
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                .header("Authorization", "Bearer %s".formatted(System.getenv("OPENAI_API_KEY")))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(chatRequest)))
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println(response.statusCode());
        String body = response.body();
        System.out.println(body);

        ChatResponse chatResponse = gson.fromJson(body, ChatResponse.class);
        System.out.println(chatResponse);
        System.out.println(chatResponse.usage());
        System.out.println(chatResponse.choices()
                .get(0)
                .message()
                .content());
    }
}
