package com.kousenit.openai;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WhisperAITest {
    private final static String SAMPLE_WAV_FILE = "src/main/resources/AssertJExceptions.wav";
    private static final String SAMPLE_LARGE_WAV_FILE = "src/main/resources/EarningsCall.wav";

    private final WhisperAI whisperAI = new WhisperAI();

    @Test
    void transcribeFileBelowSizeLimit() {
        assertTrue(whisperAI.transcribe(SAMPLE_WAV_FILE));
    }

    @Test
    void transcribeFileAboveSizeLimit() {
        assertTrue(whisperAI.transcribe(SAMPLE_LARGE_WAV_FILE));
    }

}