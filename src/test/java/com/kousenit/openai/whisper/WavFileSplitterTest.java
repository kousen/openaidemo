package com.kousenit.openai.whisper;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.withinPercentage;

class WavFileSplitterTest {

    // Size: 4.2MB
    private static final String SMALL_WAV_FILE = "src/main/resources/audio/AssertJExceptions.wav";

    // Size: 48MB
    private static final String MEDIUM_WAV_FILE = "src/main/resources/audio/EarningsCall.wav";

    private final WavFileSplitter splitter = new WavFileSplitter();

    @Test
    void splitSmallFile() {
        File sourceWavFile = new File(SMALL_WAV_FILE);
        System.out.println("Source file size: " + sourceWavFile.length());
        List<File> chunks = splitter.splitWavFileIntoChunks(sourceWavFile);
        assertThat(chunks).hasSize(1);
    }

    @Test
    void splitMediumFile() {
        File sourceWavFile = new File(MEDIUM_WAV_FILE);
        System.out.println("Source file size: " + sourceWavFile.length());
        assertThat(sourceWavFile.length()).isGreaterThan(WhisperTranscribe.MAX_ALLOWED_SIZE);

        List<File> chunks = splitter.splitWavFileIntoChunks(sourceWavFile);
        chunks.forEach(chunk ->
                assertThat(chunk.length()).isLessThanOrEqualTo(WhisperTranscribe.MAX_ALLOWED_SIZE));
        System.out.println("Number of chunks: " + chunks.size());
        long totalChunkSize = chunks.stream()
                .mapToLong(File::length)
                .sum();
        System.out.println("Total chunk size: " + totalChunkSize);
        assertThat(totalChunkSize).isCloseTo(sourceWavFile.length(), withinPercentage(1.0));
    }
}