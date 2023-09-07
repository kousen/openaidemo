package com.kousenit.openai;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kousenit.openai.json.ChatRequest;
import com.kousenit.openai.json.ChatResponse;
import com.kousenit.openai.json.Message;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

public class AsyncQuizGeneratorDemo {
    private final HttpClient client = HttpClient.newHttpClient();

    private final Message systemMessage = new Message(Role.SYSTEM, """
            Create a multiple-choice quiz about the Spring Framework topic in the
            next message. The quiz should have between 3 and 6 questions for each topic.
            Each question should have four possible answers, with only one correct answer per question.
            Label the answers A through D, and identify which answer is correct.
                            
            After each question, add a section labeled [Rationales] that explains
            each of the potential answers, again labeled A through D to identify which
            rationale goes with which answer.
            """);

    private final Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .registerTypeAdapter(Role.class, new LowercaseEnumSerializer())
            .create();

    private final List<String> topicList = List.of(
            "ApplicationContext",
            "Dependency Injection",
            "Java-based configuration",
            "Testing with MockMVC"
    );

    public ChatRequest createChatRequest(String prompt) {
        return new ChatRequest("gpt-3.5-turbo",
                List.of(systemMessage, new Message(Role.USER, prompt)),
                0.7);
    }

    public void doAsyncRequests() {
        // Create a concurrent queue of question topics.
        ConcurrentLinkedQueue<String> topics = new ConcurrentLinkedQueue<>(topicList);

        long start = System.currentTimeMillis();

        // Create a list to hold Future objects
        List<CompletableFuture<Void>> futures = topics.parallelStream()
                .map(this::getCompletableFuture)
                .toList();

        // Wait until all requests done
        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();

        long end = System.currentTimeMillis();
        System.out.println("Elapsed time: " + (end - start) + " ms");
    }

    private CompletableFuture<Void> getCompletableFuture(String topic) {
        HttpRequest request = createHttpRequest(topic);
        return getAsyncResponse(topic, request);
    }

    private CompletableFuture<Void> getAsyncResponse(String topic, HttpRequest request) {
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApplyAsync(body -> gson.fromJson(body, ChatResponse.class))
                .thenAcceptAsync(response -> writeResponseToFile(topic, response));
    }

    private static void writeResponseToFile(String topic, ChatResponse response) {
        System.out.printf("%s (%s) %s %n", topic, Thread.currentThread().getName(), response.usage());
        String questions = response.choices().get(0).message().content();
        try {
            Files.writeString(
                    Path.of("build/resources/main/%s.txt".formatted(topic)),
                    questions,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private HttpRequest createHttpRequest(String topic) {
        return HttpRequest.newBuilder()
                .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                .header("Authorization", "Bearer %s".formatted(System.getenv("OPENAI_API_KEY")))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(createChatRequest(topic))))
                .build();
    }

    public static void main(String[] args) {
        AsyncQuizGeneratorDemo demo = new AsyncQuizGeneratorDemo();
        demo.doAsyncRequests();
    }
}
