package com.kousenit;

import com.kousenit.openai.*;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ChatGPTTest {
    private final ChatGPT chat = new ChatGPT();

    @Test
    void test_response_from_documentation() {
        String prompt = "Say this is a test!";
        String response = chat.getResponse(prompt);
        assertEquals("This is a test!", response);
    }

    @Test
    void suggest_a_name() {
        String prompt = """
                    Suggest a name for a presentation about
                    AI tools for Java developers.
                    """;
        String response = chat.getResponse(prompt);
        System.out.println(response);
    }

    @Test
    void read_file() {
        String data = chat.readFile("src/main/resources/graal.srt");
        System.out.println(data.length());
    }

    @Test
    void generate_quiz_questions_from_transcript() {
        Message systemMessage = new Message(Role.SYSTEM, """
                The next message contains the transcript of a video
                about using the GraalVM native-image tool to create
                a native executable from a Java program.
                
                Create a multiple-choice quiz about the text.
                The quiz should have three questions with four
                possible answers each, with only one correct
                answer per question.
                
                Label the answers A through D, and identify
                which answer is correct.
                
                After that, add a section that explains all the answers,
                again labeled A through D.
                """);
        Message userMessage = new Message(Role.USER,
                chat.readFile("src/main/resources/graal.srt"));
        ChatRequest request = new ChatRequest("gpt-3.5-turbo",
                List.of(systemMessage, userMessage),
                0.7);
        ChatResponse response = chat.createChatResponse(request);
        System.out.println(response.usage());
        System.out.println(response.choices().get(0).message().content());
    }
}