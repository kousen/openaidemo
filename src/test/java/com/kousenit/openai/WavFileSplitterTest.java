package com.kousenit.openai;

import org.junit.jupiter.api.Test;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WavFileSplitterTest {
    private static final String SAMPLE_LARGE_WAV_FILE = "src/main/resources/EarningsCall.wav";

    @Test
    void splitLargerFile() {
        File sourceWavFile = new File(SAMPLE_LARGE_WAV_FILE);
        System.out.println("Source file size: " + sourceWavFile.length());
        assertThat(sourceWavFile.length()).isGreaterThan(25 * 1024 * 1024);

        WavFileSplitter splitter = new WavFileSplitter();
        try {
            List<byte[]> chunks = splitter.splitWavFileIntoChunks(sourceWavFile);
            chunks.forEach(chunk ->
                    assertThat(chunk.length).isLessThanOrEqualTo(20 * 1024 * 1024));
        } catch (UnsupportedAudioFileException | IOException ex) {
            throw new RuntimeException(ex);
        }
    }

}