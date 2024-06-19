package com.kousenit.openai.finetune;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class UseFineTunedModel {
    private static final String API_KEY = System.getenv("OPENAI_API_KEY");

    public static void main(String[] args) throws IOException, InterruptedException {
        String prompt = "What is f(1.5)?";
        String model = "ft-your-fine-tuned-model"; // Replace with the actual model ID

        JsonObject json = new JsonObject();
        json.addProperty("model", model);
        json.addProperty("prompt", prompt);
        json.addProperty("max_tokens", 60);

        var request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openai.com/v1/completions"))
                .header("Authorization", "Bearer " + API_KEY)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                .build();

        try (var httpClient = HttpClient.newHttpClient()) {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();
            String predictedOutput = jsonResponse.getAsJsonArray("choices")
                    .get(0).getAsJsonObject().get("text").getAsString().strip();
            System.out.println("Predicted Output: " + predictedOutput);
        }
    }
}