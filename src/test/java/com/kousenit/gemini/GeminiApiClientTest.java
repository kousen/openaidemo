package com.kousenit.gemini;

import com.kousenit.utilities.PDFTextExtractor;
import org.apache.tika.exception.TikaException;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.kousenit.gemini.GeminiRecords.*;
import static org.assertj.core.api.Assertions.*;

@EnabledIfEnvironmentVariable(named = "GOOGLEAI_API_KEY", matches = ".+")
public class GeminiApiClientTest {
    private final GeminiApiClient client = new GeminiApiClient();

    @Test
    public void testMockitoMadeClear() throws Exception {
        var book = new Content(List.of(
                new Part(readMockitoBook(), null, null, null, null)
        ), "user");
        var systemInstruction = new Content(List.of(
                new Part("""
                        You have been given the contents of the book "Mockito Made Clear".
                        Please answer the following questions based on the book.
                        If the answer is not in the book, please indicate that.
                        """, null, null, null, null)), "user");
        var cachedContent = new CachedContent(List.of(book),
                null, null, null, null,
                null, "600s", null, "Mockito Made Clear",
                "models/gemini-1.5-flash-001",
                systemInstruction,
                null
        );

        // Create the cached content
        ApiResult<CachedContent> result = client.createCachedContent(cachedContent);

        assertThat(result)
                .isInstanceOf(ApiResult.Success.class)
                .extracting("data", InstanceOfAssertFactories.type(CachedContent.class))
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
                "Please summarize the three most important points.",
                "How do you mock static methods?",
                "What is the difference between @Mock and @InjectMocks?",
                "What is the difference between @Mock and @Spy?",
                "When should you NOT use mocks or stubs?",
                "What is the difference between a mock and a stub?"
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
                                        new GenerationConfig(null, null,
                                                1000, 0.7, 0.9, 40),
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