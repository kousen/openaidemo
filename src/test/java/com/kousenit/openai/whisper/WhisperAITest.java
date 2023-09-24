package com.kousenit.openai.whisper;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WhisperAITest {
    private final static String SAMPLE_SMALL_WAV_FILE = "src/main/resources/AssertJExceptions.wav";
    private static final String SAMPLE_MEDIUM_WAV_FILE = "src/main/resources/EarningsCall.wav";

    private final WhisperAI whisperAI = new WhisperAI();

    @Test
    void transcribeFileBelowSizeLimit() {
        String transcription = whisperAI.transcribe(SAMPLE_SMALL_WAV_FILE);
        assertThat(transcription)
                .isNotBlank()
                .containsIgnoringCase("AssertJ")
                .hasLineCount(1);
    }

    @Test
    void transcribeFileAboveSizeLimit() {
        String transcription = whisperAI.transcribe(SAMPLE_MEDIUM_WAV_FILE);
        assertThat(transcription)
                .isNotBlank()
                .containsIgnoringCase("FinTech Plus")
                .hasLineCount(3);
    }

}