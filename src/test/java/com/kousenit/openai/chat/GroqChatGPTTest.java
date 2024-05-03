package com.kousenit.openai.chat;

import com.kousenit.openai.json.ChatRequest;
import com.kousenit.openai.json.ChatResponse;
import com.kousenit.openai.json.Message;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class GroqChatGPTTest {
    private final GroqChatGPT chat = new GroqChatGPT();

    @Test
    void weather_in_bangalore() {
        String prompt = "What the weather report for Bangalore, India, today?";
        String response = chat.getResponse(prompt, GroqChatGPT.LLAMA3);
        System.out.println(response);
    }

    @Test
    void run_from_provided_request() {
        Message userMessage = new Message(Role.USER,
                """
                        What is the Ultimate Answer to
                        the Ultimate Question of Life,
                        the Universe, and Everything?
                        """);
        ChatRequest request = new ChatRequest(
                GroqChatGPT.MIXTRAL,
                List.of(userMessage),
                ChatGPT.DEFAULT_TEMPERATURE);
        ChatResponse response = chat.createChatResponse(request);
        System.out.println(response.usage());
        String result = chat.extractStringResponse(response);
        System.out.println(result);
        assertThat(result).contains("42");
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {GroqChatGPT.LLAMA3, GroqChatGPT.MIXTRAL, GroqChatGPT.GEMMA})
    void sum_word_lengths(String model) {
        Message userMessage = new Message(Role.USER, """
            What is the sum of the lengths of the words in the sentence
            "The quick brown fox jumped over the lazy dog"?
            Show all your steps.
            """);
        ChatRequest request = new ChatRequest(
                model,
                List.of(userMessage),
                ChatGPT.DEFAULT_TEMPERATURE);
        long startTime = System.nanoTime();
        ChatResponse response = chat.createChatResponse(request);
        long endTime = System.nanoTime();
        System.out.println("Elapsed time: " + (endTime - startTime) / 1_000_000 + " ms");
        System.out.println(response.usage());
        String result = chat.extractStringResponse(response);
        System.out.println(result);
        assertThat(result).contains("36");
    }
}