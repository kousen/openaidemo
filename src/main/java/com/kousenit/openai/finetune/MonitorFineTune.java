package com.kousenit.openai.finetune;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.io.IOException;

public class MonitorFineTune {
    private static final String API_KEY = System.getenv("OPENAI_API_KEY");

    public static void main(String[] args) {
        String fineTuneJobId = "ft-your-fine-tune-job-id"; // Replace with the actual fine-tune job ID

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            checkFineTuneJobStatus(httpClient, fineTuneJobId);
            listFineTuneEvents(httpClient, fineTuneJobId);
        } catch (IOException e) {
            System.err.println("Error processing fine-tune job: " + e.getMessage());
        }
    }

    private static void checkFineTuneJobStatus(CloseableHttpClient httpClient, String fineTuneJobId) throws IOException {
        String url = String.format("https://api.openai.com/v1/fine_tuning/jobs/%s", fineTuneJobId);

        var httpGet = new HttpGet(url);
        httpGet.setHeader("Authorization", "Bearer " + API_KEY);

        HttpClientResponseHandler<String> responseHandler = response -> {
            int status = response.getCode();
            if (status >= 200 && status < 300) {
                return EntityUtils.toString(response.getEntity());
            } else {
                throw new IOException("Unexpected response status: " + status);
            }
        };

        String responseBody = httpClient.execute(httpGet, responseHandler);
        JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
        String jobStatus = jsonResponse.get("status").getAsString();

        System.out.println("Fine-tune job status: " + jobStatus);

        if ("succeeded".equals(jobStatus)) {
            String fineTunedModel = jsonResponse.get("fine_tuned_model").getAsString();
            System.out.println("Fine-tuning job completed successfully. Fine-tuned model: " + fineTunedModel);
        } else {
            System.out.println("Fine-tuning job is not yet completed or has failed.");
        }
    }

    private static void listFineTuneEvents(CloseableHttpClient httpClient, String fineTuneJobId) throws IOException {
        String url = String.format("https://api.openai.com/v1/fine_tuning/jobs/%s/events", fineTuneJobId);

        var httpGet = new HttpGet(url);
        httpGet.setHeader("Authorization", "Bearer " + API_KEY);

        HttpClientResponseHandler<String> responseHandler = response -> {
            int status = response.getCode();
            if (status >= 200 && status < 300) {
                return EntityUtils.toString(response.getEntity());
            } else {
                throw new IOException("Unexpected response status: " + status);
            }
        };

        String responseBody = httpClient.execute(httpGet, responseHandler);
        JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
        JsonArray events = jsonResponse.getAsJsonArray("data");

        System.out.println("Fine-tune events for job ID: " + fineTuneJobId);
        for (int i = 0; i < events.size(); i++) {
            JsonObject event = events.get(i).getAsJsonObject();
            String eventId = event.get("id").getAsString();
            String message = event.get("message").getAsString();
            long createdAt = event.get("created_at").getAsLong();
            String level = event.get("level").getAsString();
            String type = event.get("type").getAsString();

            System.out.printf("Event ID: %s%nMessage: %s%nCreated At: %d%nLevel: %s%nType: %s%n%n",
                    eventId, message, createdAt, level, type);
        }
    }
}