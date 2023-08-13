package com.kousenit.picogen;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

// See docs at https://picogen.io/docs
public class Picogen {
    private static final String BASEURL = "https://api.picogen.io";
    private static final String COMPLETED_STATUS = "completed";
    private static final int SLEEP_TIME_MILLIS = 1000;

    private final HttpClient client = HttpClient.newHttpClient();

    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    public record JobResponse(String id, int cost) {}

    // status:  'created', 'processing', 'completed', or 'error'
    public record GetResponse(long id, long accountId, long tokenId, long transactionId,
                          String status, Payload payload, List<String> result, int durationMs,
                          long createdAt, long updatedAt) {
        public record Payload(Options options, String model, String command, int version){
            public record Options(String size, int count, String style,
                                  String engine, String prompt) {}
        }
    }

    public record ListResponse(List<GetResponse> items) {}

    public record GenerateRequest(int version, int count, String model, String command,
                          String prompt, String ratio, String style, String engine) {}

    // model:
    //      'stability', 'midjourney', 'dalle2'
    // engine:
    //      Stability: xl-v1.0, 512-v2.1, 768-v2.1, v1.5
    //      Default: xl-v1.0
    //      Midjourney: mj-4, mj-5.1, mj-5.1-raw, mj-5.2, mj-5.2-raw, niji-4, niji-5,
    //                  niji-5-cute, niji-5-expressive, niji-5-original, niji-5-scenic
    //      Default: mj-5.2
    public GenerateRequest createJobRequest(String model, String prompt, String engine) {
        return new GenerateRequest(1, 1, model, "generate", prompt,
                "16:9", "photographic", engine);
    }

    public JobResponse doJob(GenerateRequest generateRequest) {
        String json = gson.toJson(generateRequest);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASEURL + "/job/run"))
                .header("API-Token", System.getenv("PICOGEN_API_KEY"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        try {
            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());
            String string = response.body();
            System.out.println("Server response: " + string); // Keep this line for debugging
            List<JobResponse> jobResponses = gson.fromJson(string,
                    new TypeToken<List<JobResponse>>(){}.getType());
            JobResponse jobResponse = jobResponses.get(1);
            System.out.printf("ID: %s, cost: %d%n", jobResponse.id(), jobResponse.cost());
            return jobResponse;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> getJobList() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASEURL + "/job/list/"))
                .header("API-Token", System.getenv("PICOGEN_API_KEY"))
                .header("Content-Type", "application/json")
                .GET()
                .build();
        try {
            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());
            String string = response.body();
            List<ListResponse> listResponses = gson.fromJson(string,
                    new TypeToken<List<ListResponse>>(){}.getType());
            return listResponses.stream()
                    .filter(Objects::nonNull)
                    .flatMap(listResponse -> listResponse.items().stream())
                    .filter(items -> items.status().equals("completed"))
                    .map(getResponse -> getResponse.result().get(0))
                    .collect(Collectors.toList());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public GetResponse getResponse(JobResponse jobResponse) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASEURL + "/job/get/" + jobResponse.id()))
                .header("API-Token", System.getenv("PICOGEN_API_KEY"))
                .header("Content-Type", "application/json")
                .GET()
                .build();
        try {
            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());
            String string = response.body();
            System.out.println("Server response: " + string); // Keep this line for debugging

            // Parse the response as a List of GetResponse
            List<GetResponse> getResponses = gson.fromJson(string,
                    new TypeToken<List<GetResponse>>(){}.getType());

            // Get the first non-null response
            return getResponses.stream()
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No valid response"));
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public GetResponse waitForResponseCompletion(JobResponse jobResponse) {
        GetResponse response = getResponse(jobResponse);
        while (!isResponseCompleted(response)) {
            // System.out.println(response.status() + "...");
            sleepInterruptibly();
            response = getResponse(jobResponse);
        }
        System.out.println("Completed in " + response.durationMs() + " ms");
        return response;
    }

    private boolean isResponseCompleted(GetResponse response) {
        return response.status().equals(COMPLETED_STATUS);
    }

    private void sleepInterruptibly() {
        try {
            Thread.sleep(SLEEP_TIME_MILLIS);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) {
        Picogen picogen = new Picogen();
        GenerateRequest jobRequest = picogen.createJobRequest(
                "stability",
                """
                Captain James T. Kirk in a light-saber battle
                with a Klingon on the bridge of the Enterprise.
                """,
                "xl-v1.0");
        JobResponse jobResponse = picogen.doJob(jobRequest);
        GetResponse response = picogen.waitForResponseCompletion(jobResponse);
        System.out.println(response.result().get(0));
    }
}
