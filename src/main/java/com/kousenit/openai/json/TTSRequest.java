package com.kousenit.openai.json;

import com.google.gson.annotations.SerializedName;
import com.kousenit.openai.tts.TextToSpeech;

import java.util.Objects;

public record TTSRequest(
        String model,
        String input,
        Voice voice,
        @SerializedName("response_format")
        ResponseFormat responseFormat,
        double speed
) {
    public TTSRequest {
        // Input validation
        if (!validateModel(model)) {
            throw new IllegalArgumentException("Invalid model name: " + model);
        }
        if (!validateInput(input)) {
            throw new IllegalArgumentException("Input is null, blank, or too long");
        }
        if (!validateSpeed(speed)) {
            throw new IllegalArgumentException("Speed must be between 0.25 and 4.0");
        }
    }

    // Required parameters only -- the rest are defaults
    public TTSRequest(String model, String input, Voice voice) {
        this(model, input, voice, ResponseFormat.MP3, 1.0);
    }

    private boolean validateInput(String input) {
        return input != null && !input.isBlank() && input.length() <= 4096;
    }

    private boolean validateModel(String model) {
        return Objects.equals(model, TextToSpeech.TTS_1) ||
               Objects.equals(model, TextToSpeech.TTS_1_HD);
    }

    private boolean validateSpeed(double speed) {
        return speed >= 0.25 && speed <= 4.0;
    }
}
