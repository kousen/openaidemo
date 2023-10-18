package com.kousenit.openai.whisper;

import com.kousenit.openai.chat.ChatGPT;
import com.kousenit.openai.chat.Role;
import com.kousenit.openai.json.Message;
import com.kousenit.utilities.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import static com.kousenit.openai.whisper.TutorialPrompts.*;

// https://platform.openai.com/docs/tutorials/meeting-minutes
// Transcribe and analyze meeting minutes tutorial
public class WhisperTutorial {
    private static final String RESOURCES_PATH = "src/main/resources/";

    private final WhisperTranscribe whisperTranscribe = new WhisperTranscribe();
    private final ChatGPT chatGPT = new ChatGPT();

    public void processMeetingMinutes() {
        // Transcribe audio, or load transcription if it already exists
        String transcription = getTranscription("EarningsCall");

        Map<String, String> promptMap = Map.ofEntries(
                Map.entry("summarize", SUMMARIZE_PROMPT),
                Map.entry("key_points", KEY_POINTS_PROMPT),
                Map.entry("action_items", ACTION_ITEMS_PROMPT),
                Map.entry("sentiment", SENTIMENT_PROMPT)
        );

        // Call GPT-4 to get the responses to each prompt, in parallel
        ConcurrentMap<String, String> responseMap = promptMap.entrySet()
                .parallelStream()
                .collect(Collectors.toConcurrentMap(
                                Map.Entry::getKey,
                                e -> getResponse(e.getValue(), transcription)
                        )
                );

        responseMap.forEach((name, response) ->
                FileUtils.writeTextToFile(response, name + ".txt"));
    }

    private String getResponse(String prompt, String transcription) {
        return chatGPT.getResponseToMessages(ChatGPT.GPT_4,
                new Message(Role.SYSTEM, prompt),
                new Message(Role.USER, transcription));
    }

    @SuppressWarnings("SameParameterValue")
    public String getTranscription(String fileName) {
        Path transcriptionFilePath = Paths.get(RESOURCES_PATH, "text", fileName + ".txt");
        Path audioFilePath = Paths.get(RESOURCES_PATH, "audio", fileName + ".wav");

        if (Files.exists(transcriptionFilePath)) {
            try {
                return Files.readString(transcriptionFilePath);
            } catch (IOException e) {
                System.err.println("Error reading transcription file: " + e.getMessage());
            }
        } else {
            return whisperTranscribe.transcribe(audioFilePath.toString());
        }
        return "";
    }
}
