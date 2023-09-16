package com.kousenit.openai;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WhisperAITest {
    private final WhisperAI whisperAI = new WhisperAI();

    @Test
    void transcribe() {
        assertTrue(whisperAI.transcribe(WhisperAI.SAMPLE_WAV_FILE));
    }
}