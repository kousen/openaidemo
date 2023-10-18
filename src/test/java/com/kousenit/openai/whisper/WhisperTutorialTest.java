package com.kousenit.openai.whisper;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WhisperTutorialTest {
    private final WhisperTutorial tutorial = new WhisperTutorial();

    @Test
    void processMeetingMinutes() {
        tutorial.processMeetingMinutes();
    }

    @Test
    void getTranscription() {
        String transcription = tutorial.getTranscription("EarningsCall");
        assertThat(transcription).isNotBlank();
    }
}