package com.kousenit.openai.whisper;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
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
        List<File> chunks = splitter.splitWavFileIntoChunks(sourceWavFile);
        for (int i = 0; i < chunks.size(); i++) {
            File chunk = chunks.get(i);
            assertThat(chunk.length()).isLessThanOrEqualTo(25 * 1024 * 1024);
            System.out.println("File: " + chunk.getName() + ", Chunk size: " + chunk.length());
            String outputFileName = String.format("%s/chunk_%d.wav", "src/main/resources", i);
            try (FileOutputStream fos = new FileOutputStream(outputFileName)) {
                fos.write(Files.readAllBytes(chunk.toPath()));
            } catch (IOException e) {
                System.err.println("Failed to write chunk to file: " + e.getMessage());
            }
        }
    }

}