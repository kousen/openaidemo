package com.kousenit.gemini;

import com.kousenit.utilities.PDFTextExtractor;
import org.apache.tika.exception.TikaException;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.kousenit.gemini.GeminiRecords.*;
import static org.assertj.core.api.Assertions.*;

public class GeminiApiClientTest {
    private final GeminiApiClient client = new GeminiApiClient();

    @Test
    public void testCreateCachedContent() throws Exception {
        // Create a sample CachedContent instance
        Part part = new Part(readMockitoBook(), null, null, null, null);
        Content content = new Content(List.of(part), "user");
        CachedContent cachedContent = new CachedContent(
                List.of(content), null, null, null, null,
                null, "600s", null, "Mockito Made Clear",
                "models/gemini-1.5-flash-001", null, null
        );

        // Call the createCachedContent method
        ApiResult<CachedContent> result = client.createCachedContent(cachedContent);

        assertThat(result)
                .as("CreateCachedContent result")
                .isInstanceOf(ApiResult.Success.class)
                .extracting("data", InstanceOfAssertFactories.type(CachedContent.class))
                .as("Created content")
                .isNotNull()
                .satisfies(createdContent -> {
                    System.out.println(createdContent);
                    askQuestionsFromCache(createdContent);
                });
    }

    private void askQuestionsFromCache(CachedContent cachedContent) {
        List<String> questions = List.of(
                "How many chapters are in the book?",
                "What is the main theme of the book?",
                "Please summarize the three most important points."
        );

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<ApiResult<GenerateContentResponse>>> futures =
                    questions.stream()
                            .map(question -> executor.submit(() -> {
                                var questionPart = new Part(question, null,
                                        null, null, null);
                                var request = new GenerateContentRequest(
                                        List.of(new Content(List.of(questionPart), "user")),
                                        null, null, null, null,
                                        new GenerationConfig(null, null, 1000, 0.7, 0.9, 40),
                                        cachedContent.name()
                                );
                                return client.generateContent("gemini-1.5-flash-001", request);
                            }))
                            .toList();

            assertThat(futures)
                    .allSatisfy(future -> {
                        ApiResult<GenerateContentResponse> result;
                        try {
                            result = future.get();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            throw new AssertionError("Test was interrupted", e);
                        } catch (Exception e) {
                            throw new AssertionError("Error getting future result", e);
                        }

                        assertThat(result)
                                .as("GenerateContent result")
                                .isInstanceOf(ApiResult.Success.class)
                                .extracting("data", InstanceOfAssertFactories.type(GenerateContentResponse.class))
                                .as("Generated content response")
                                .isNotNull()
                                .satisfies(System.out::println);
                    });
        }
    }

    @Test
    public void testListCachedContents() {
        // Call the listCachedContents method
        ApiResult<List<CachedContent>> result = client.listCachedContents(10, null);

        assertThat(result)
                .as("ListCachedContents result")
                .isNotNull()
                .isInstanceOf(ApiResult.Success.class);

        if (result instanceof ApiResult.Success<List<CachedContent>> success) {
            List<CachedContent> cachedContents = success.data();
            assertThat(cachedContents)
                    .as("List of cached contents")
                    .isNotNull();

            if (cachedContents.isEmpty()) {
                System.out.println("No cached contents found");
            } else {
                cachedContents.forEach(System.out::println);
            }
        } else {
            fail("Expected Success, but got: " + result);
        }
    }

    private String readMockitoBook() throws IOException, TikaException, SAXException {
        return PDFTextExtractor.extractText(
                "src/main/resources/pdfs/mockito-made-clear_P1.0.pdf");
    }
}