package com.kousenit.openai.whisper;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WhisperTranslateTest {
    private final static String SAMPLE_SMALL_WAV_FILE =
            "src/main/resources/audio/assertj_hindi.wav";

    private final WhisperTranslate whisperTranscribe = new WhisperTranslate();

    @Test
    void transcribeFileBelowSizeLimit() {
        String translation = whisperTranscribe.translate(SAMPLE_SMALL_WAV_FILE);
        assertThat(translation)
                .isNotBlank()
                .hasLineCount(1);
    }

}