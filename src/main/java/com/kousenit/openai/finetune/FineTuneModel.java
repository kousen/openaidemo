package com.kousenit.openai.finetune;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.io.IOException;

public class FineTuneModel {
    private static final String API_KEY = System.getenv("OPENAI_API_KEY");

    public static void main(String[] args) {
        String trainingFileId = "...";
        String model = "gpt-3.5-turbo";

        var json = new JsonObject();
        json.addProperty("training_file", trainingFileId);
        json.addProperty("model", model);

        var httpPost = new HttpPost("https://api.openai.com/v1/fine_tuning/jobs");
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setHeader("Authorization", "Bearer " + API_KEY);

        HttpEntity entity = new StringEntity(json.toString(), ContentType.APPLICATION_JSON);
        httpPost.setEntity(entity);

        HttpClientResponseHandler<String> responseHandler = response -> {
            int status = response.getCode();
            if (status >= 200 && status < 300) {
                HttpEntity responseEntity = response.getEntity();
                return responseEntity != null ? EntityUtils.toString(responseEntity) : null;
            } else {
                throw new IOException("Unexpected response status: " + status);
            }
        };

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            String responseBody = httpClient.execute(httpPost, responseHandler);
            JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
            String fineTuneId = jsonResponse.get("id").getAsString();
            System.out.println("Fine-tune job ID: " + fineTuneId);
        } catch (IOException e) {
            System.err.println("Error creating fine-tune job: " + e.getMessage());
        }
    }
}