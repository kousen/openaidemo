package com.kousenit.openai.whisper;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WhisperTranscribeTest {
    private final static String SAMPLE_SMALL_WAV_FILE = "src/main/resources/audio/AssertJExceptions.wav";
    private static final String SAMPLE_MEDIUM_WAV_FILE = "src/main/resources/audio/EarningsCall.wav";

    private final WhisperTranscribe whisperTranscribe = new WhisperTranscribe();

    @Test
    void transcribeFileBelowSizeLimit() {
        String transcription = whisperTranscribe.transcribe(SAMPLE_SMALL_WAV_FILE);
        assertThat(transcription)
                .isNotBlank()
                .contains("AssertJ")
                .hasLineCount(1);
    }

    @Test
    void transcribeFileAboveSizeLimit() {
        String transcription = whisperTranscribe.transcribe(SAMPLE_MEDIUM_WAV_FILE);
        assertThat(transcription)
                .isNotBlank()
                .contains("FinTech Plus")
                .hasLineCount(3);
    }

}