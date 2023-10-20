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

// https://platform.openai.com/docs/tutorials/meeting-minutes
// Transcribe and analyze meeting minutes tutorial
public class WhisperTutorial {

    private final WhisperTranscribe whisperTranscribe = new WhisperTranscribe();
    private final ChatGPT chatGPT = new ChatGPT();

    public void processMeetingMinutes() {
        // Transcribe audio, or load transcription if it already exists
        String transcription = getTranscription("EarningsCall");

        Map<String, String> promptMap = Map.ofEntries(
                Map.entry("summarize", TutorialPrompts.SUMMARIZE_PROMPT),
                Map.entry("key_points", TutorialPrompts.KEY_POINTS_PROMPT),
                Map.entry("action_items", TutorialPrompts.ACTION_ITEMS_PROMPT),
                Map.entry("sentiment", TutorialPrompts.SENTIMENT_PROMPT)
        );

        // Call GPT-4 to get the responses to each prompt
        long startTime = System.nanoTime();
        ConcurrentMap<String, String> responseMap = promptMap.entrySet().parallelStream()
                .peek(e -> System.out.println("Processing " + e.getKey()))
                .collect(Collectors.toConcurrentMap(
                                Map.Entry::getKey,
                                e -> getResponse(e.getValue(), transcription)
                        )
                );
        long endTime = System.nanoTime();
        System.out.printf("Elapsed time: %.3f seconds%n", (endTime - startTime) / 1e9);

        responseMap.forEach((name, response) ->
                FileUtils.writeTextToFile(response, name + ".txt"));
    }

    public String getResponse(String prompt, String transcription) {
        return chatGPT.getResponseToMessages(ChatGPT.GPT_4,
                new Message(Role.SYSTEM, prompt),
                new Message(Role.USER, transcription));
    }

    @SuppressWarnings("SameParameterValue")
    public String getTranscription(String fileName) {
        Path transcriptionFilePath = Paths.get(FileUtils.TEXT_RESOURCES_PATH, fileName + ".txt");
        Path audioFilePath = Paths.get(FileUtils.AUDIO_RESOURCES_PATH, fileName + ".wav");

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
