package com.kousenit.gemini;

import com.kousenit.utilities.PDFTextExtractor;
import org.apache.tika.exception.TikaException;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.List;

import static com.kousenit.gemini.GeminiRecords.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class GeminiApiClientTest {

    @Test
    public void testCreateCachedContent() throws Exception {
        // Create a sample CachedContent instance
        Part part = new Part(readMockitoBook(),
                null, null, null, null);
        Content content = new Content(List.of(part), "user");
        CachedContent cachedContent = new CachedContent(
                List.of(content),
                null, null, null, null,
                null, "600s", null, "Mockito Made Clear",
                "models/gemini-1.5-flash-001", null, null
        );

        // Call the createCachedContent method
        CachedContent createdContent = GeminiApiClient.createCachedContent(cachedContent);
        assertNotNull(createdContent);
        System.out.println(createdContent);

        askQuestionsFromCache(createdContent);
    }

    private void askQuestionsFromCache(CachedContent cachedContent) {
        List<String> questions = List.of(
                "How many chapters are in the book?",
                "What is the main theme of the book?",
                "Please summarize the three most important points."
        );

        questions.forEach(question -> {
                    var questionPart = new Part(question,
                            null, null, null, null);
                    var request = new GenerateContentRequest(
                            List.of(new Content(List.of(questionPart), "user")),
                            null, null, null, null,
                            new GenerationConfig(null, null,
                                    1000, 0.7, 0.9, 40),
                            cachedContent.name()
                    );

                    // Call the generateContent method
                    var response = GeminiApiClient.generateContent("gemini-1.5-flash-001", request);
                    assertNotNull(response);
                    System.out.println(response);
                }
        );
    }

    @Test
    public void testListCachedContents() throws Exception {
        // Call the listCachedContents method
        List<CachedContent> cachedContents = GeminiApiClient.listCachedContents(10, null);
        if (cachedContents != null) {
            cachedContents.forEach(System.out::println);
        } else {
            System.out.println("No cached contents found");
        }
    }

    private String readMockitoBook() throws IOException, TikaException, SAXException {
        return PDFTextExtractor.extractText(
                "src/main/resources/pdfs/mockito-made-clear_P1.0.pdf");
    }
}