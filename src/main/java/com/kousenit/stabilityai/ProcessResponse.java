package com.kousenit.stabilityai;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.FileReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;

public class ProcessResponse {
    public static void main(String[] args) throws IOException {
        Gson gson = new Gson();

        record Image(String base64, int seed, String finishReason) {
        }

        record Artifacts(List<Image> artifacts) {
        }

        try (JsonReader reader = new JsonReader(new FileReader("response.json"))) {
            Artifacts artifacts = gson.fromJson(reader, Artifacts.class);
            artifacts.artifacts().forEach(artifact -> {
                System.out.println(artifact.seed() + ": " + artifact.finishReason());
                byte[] bytes = Base64.getDecoder().decode(artifact.base64());
                try {
                    Files.write(Path.of("prompt" + artifact.seed() + ".png"), bytes);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        }
    }
}

