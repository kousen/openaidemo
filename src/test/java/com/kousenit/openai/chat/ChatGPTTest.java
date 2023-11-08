package com.kousenit.openai.chat;

import com.kousenit.utilities.FileUtils;
import com.kousenit.openai.json.ChatRequest;
import com.kousenit.openai.json.ChatResponse;
import com.kousenit.openai.json.Message;
import com.kousenit.openai.json.ModelList;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ChatGPTTest {
    private final ChatGPT chat = new ChatGPT();

    @Test
    void list_models() {
        List<ModelList.Model> models = chat.listModels();
        assertThat(models).isNotEmpty();
        List<String> ids = models.stream()
                .sorted(Comparator.comparing(ModelList.Model::id))
                .map(ModelList.Model::id)
                .distinct()
                .toList();
        ids.forEach(System.out::println);
        assertThat(ids).contains(ChatGPT.GPT_4, ChatGPT.GPT_4_TURBO);
    }

    @Test
    void list_gpt_models() {
        List<ModelList.Model> models = chat.listModels();
        List<ModelList.Model> gptModels = models.stream()
                .filter(model -> model.id().contains("gpt"))
                .peek(System.out::println)
                .toList();
        assertThat(gptModels)
                .map(ModelList.Model::id)
                .contains("gpt-3.5-turbo", "gpt-4");
    }

    @Test
    void list_all_models() {
        List<ModelList.Model> models = chat.listModels();
        List<ModelList.Model> gptModels = models.stream()
                .peek(System.out::println)
                .sorted(Comparator.comparing(ModelList.Model::created))
                .toList();
        assertThat(gptModels)
                .map(ModelList.Model::id)
                .contains("gpt-3.5-turbo", "gpt-4");
    }

    @Test
    void test_response_from_documentation() {
        String prompt = "Say this is a test!";
        String response = chat.getResponse(prompt, ChatGPT.GPT_35_TURBO);
        assertEquals("This is a test!", response);
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
                ChatGPT.GPT_35_TURBO,
                List.of(userMessage),
                ChatGPT.DEFAULT_TEMPERATURE);
        ChatResponse response = chat.createChatResponse(request);
        System.out.println(response.usage());
        String result = chat.extractStringResponse(response);
        System.out.println(result);
        assertThat(result).contains("42");
    }

    @Test
    void generate_quiz_questions_from_transcript() {
        Message systemMessage = new Message(Role.SYSTEM, """
                You are a Java instructor who recorded a video course,
                and need to create a quiz for the course.
                
                The next message contains the transcript of a video
                from the course about using the GraalVM native-image tool
                to create a native executable from a Java program.""");

        Message quizMessage = new Message(Role.USER,
                """                
                Create a multiple-choice quiz about the text.
                The quiz should have three questions with four
                possible answers each, with only one correct
                answer per question.
                
                Label the answers A through D, and identify
                which answer is correct.
                
                After that, add a section that explains all the answers,
                again labeled A through D.
                """);

        Message fileMessage = new Message(Role.SYSTEM,
                FileUtils.readFile("src/main/resources/graal.srt"));

        ChatRequest request = new ChatRequest(ChatGPT.GPT_4_TURBO,
                List.of(systemMessage, fileMessage, quizMessage),
                ChatGPT.DEFAULT_TEMPERATURE);
        ChatResponse response = chat.createChatResponse(request);
        System.out.println(response.usage());
        System.out.println(chat.extractStringResponse(response));
    }
}