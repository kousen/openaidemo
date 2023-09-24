package com.kousenit.openai.whisper;

import com.kousenit.openai.ChatGPT;
import com.kousenit.openai.Role;
import com.kousenit.openai.json.Message;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

// https://platform.openai.com/docs/tutorials/meeting-minutes
// Transcribe and analyze meeting minutes tutorial
public class WhisperTutorial {
    private final WhisperAI whisperAI = new WhisperAI();
    private final ChatGPT chatGPT = new ChatGPT();
    private static final String RESOURCES_PATH = "src/main/resources/";

    private static final String SUMMARIZE_PROMPT = """
            You are a highly skilled AI trained in language comprehension and summarization.
            I would like you to read the following text and summarize it into a concise
            abstract paragraph. Aim to retain the most important points, providing a coherent
            and readable summary that could help a person understand the main points of the
            discussion without needing to read the entire text. Please avoid unnecessary
            details or tangential points.
            """;

    private static final String KEY_POINTS_PROMPT = """
            You are a proficient AI with a specialty in distilling information into key points.
            Based on the following text, identify and list the main points that were discussed
            or brought up. These should be the most important ideas, findings, or topics that
            are crucial to the essence of the discussion. Your goal is to provide a list that
            someone could read to quickly understand what was talked about.
            """;

    private static final String ACTION_ITEMS_PROMPT = """
            You are an AI expert in analyzing conversations and extracting action items.
            Please review the text and identify any tasks, assignments, or actions that
            were agreed upon or mentioned as needing to be done. These could be tasks
            assigned to specific individuals, or general actions that the group has
            decided to take. Please list these action items clearly and concisely.
            """;

    private static final String SENTIMENT_PROMPT = """
            As an AI with expertise in language and emotion analysis, your task is to analyze
            the sentiment of the following text. Please consider the overall tone of the
            discussion, the emotion conveyed by the language used, and the context in which words
            and phrases are used. Indicate whether the sentiment is generally positive, negative,
            or neutral, and provide brief explanations for your analysis where possible.
            """;

    @SuppressWarnings("SameParameterValue")
    private String getTranscription(String fileName) {
        File sourceWavFile = new File(RESOURCES_PATH + fileName + ".wav");
        File transcriptionFile = new File(RESOURCES_PATH + "text/" + fileName + ".txt");

        if (transcriptionFile.exists()) {
            try {
                return Files.readString(Path.of(transcriptionFile.getAbsolutePath()));
            } catch (IOException e) {
                System.err.println("Error reading transcription file: " + e.getMessage());
            }
        } else {
            return whisperAI.transcribe(sourceWavFile.getAbsolutePath());
        }
        return "";
    }

    public void processMeetingMinutes() {
        // Only call this once:
        String transcription = getTranscription("EarningsCall");

        // Call GPT-4 in parallel to get the responses to each prompt
        List.of(SUMMARIZE_PROMPT, KEY_POINTS_PROMPT, ACTION_ITEMS_PROMPT, SENTIMENT_PROMPT).parallelStream()
                .map(prompt -> chatGPT.getResponseToMessages(ChatGPT.GPT_4,
                        new Message(Role.SYSTEM, prompt),
                        new Message(Role.USER, transcription)))
                .forEach(System.out::println);
    }

    public static void main(String[] args) throws IOException {
        new WhisperTutorial().processMeetingMinutes();
    }
}
