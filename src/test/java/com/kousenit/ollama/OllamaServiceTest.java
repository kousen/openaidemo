package com.kousenit.ollama;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;

import static com.kousenit.ollama.OllamaRecords.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class OllamaServiceTest {
    private final OllamaService service = new OllamaService();

    @Test
    void test_generate_string() {
        String response = service.generate(OllamaService.LLAMA3, "Why is the sky blue?");
        assertNotNull(response);
        System.out.println(response);
    }

    @Test
    void test_generate() {
        var request = new OllamaTextRequest(OllamaService.LLAMA3,
                """
                        Why is the sky blue?
                        """,
                false);
        OllamaResponse response = service.generate(request);
        assertNotNull(response);
        System.out.println(response);
    }

    @Test
    void test_vision_generate() {
        var request = new OllamaVisionRequest(OllamaService.LLAVA,
                "describe the included image",
                false,
                List.of("src/main/resources/images/stablediffusion/cats_playing_cards.png"));
        OllamaResponse response = service.generate(request);
        assertNotNull(response);
        System.out.println(response);
    }

    @Test
    void list_local_models() {
        OllamaModels models = service.listLocalModels();
        assertThat(models.models()).isNotEmpty();
        models.models()
                .stream()
                .sorted(Comparator.comparing(OllamaModel::modifiedAt)
                        .thenComparing(OllamaModel::name))
                .forEach(System.out::println);
    }
}