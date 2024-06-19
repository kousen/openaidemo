package com.kousenit.openai.finetune;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class GenerateTrainingData {
    public static void main(String[] args) {
        var outputFilePath = Path.of("src/main/resources/sine_training_data.jsonl");
        var gson = new Gson();

        try (var writer = Files.newBufferedWriter(outputFilePath)) {
            for (double x = 0; x <= 2 * Math.PI; x += 0.1) {
                double y = Math.sin(x);
                var jsonObject = new JsonObject();
                jsonObject.addProperty("prompt", String.format("What is f(%.1f)?", x));
                jsonObject.addProperty("completion", String.format("%.3f", y));
                writer.write(gson.toJson(jsonObject));
                writer.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Error writing to file", e);
        }
    }
}