package com.kousenit.ollama;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

public class OllamaRecords {
    public sealed interface OllamaRequest
            permits OllamaTextRequest, OllamaVisionRequest {
    }

    public record OllamaTextRequest(String model, String prompt, boolean stream)
            implements OllamaRequest {
    }

    public record OllamaVisionRequest(
            String model,
            String prompt,
            boolean stream,
            List<String> images)
            implements OllamaRequest {

        public OllamaVisionRequest {
            images = images.stream()
                    .map(this::encodeImage)
                    .collect(Collectors.toList());
        }

        private String encodeImage(String path) {
            try {
                byte[] imageBytes = Files.readAllBytes(Paths.get(path));
                return Base64.getEncoder()
                        .encodeToString(imageBytes);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }


    public record OllamaResponse(
            String model, String createdAt, String response,
            boolean done, long totalDuration, int promptEvalCount, int evalCount) {
    }

    // List models
    public record OllamaModel(
            String name,
            OffsetDateTime modifiedAt,
            long size,
            String digest,
            Details details
    ) {}

    public record Details(
            String format,
            String family,
            List<String> families,
            String parameterSize,
            String quantizationLevel
    ) {}

    public record OllamaModels(
            List<OllamaModel> models
    ) {}

    // Streaming models
    record OllamaStreamingResponse(
            String model,
            String createdAt,
            String response,
            boolean done) {
    }

    record OllamaCompletedResponse(
            String model, String createdAt, String response,
            boolean done, String doneReason,
            List<Integer> context,
            long totalDuration, long loadDuration,
            int promptEvalCount, int promptEvalDuration,
            int evalCount, int evalDuration) {
    }

}
