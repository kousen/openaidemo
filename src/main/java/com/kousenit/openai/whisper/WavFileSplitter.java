package com.kousenit.openai.whisper;

import javax.sound.sampled.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class WavFileSplitter {
    // maximum chunk size of 25MB; use 20MB to be safe
    private static final long MAX_CHUNK_SIZE_BYTES = 20 * 1024 * 1024;

    public List<File> splitWavFileIntoChunks(File sourceWavFile) {
        List<File> chunks = new ArrayList<>();
        int chunkCounter = 1;

        try (var inputStream = AudioSystem.getAudioInputStream(sourceWavFile)) {
            long totalFrames = inputStream.getFrameLength(); // Total frames in the source wav file
            AudioFileFormat fileFormat = AudioSystem.getAudioFileFormat(sourceWavFile);
            AudioFormat format = fileFormat.getFormat();

            // Calculate the maximum number of frames for each chunk
            int frameSize = format.getFrameSize(); // Number of bytes in each frame
            long framesPerChunk = MAX_CHUNK_SIZE_BYTES / frameSize;

            byte[] buffer = new byte[(int) (framesPerChunk * frameSize)];

            while (totalFrames > 0) {
                long framesInThisFile = Math.min(totalFrames, framesPerChunk);
                int bytesRead = inputStream.read(buffer, 0, (int) (framesInThisFile * frameSize));
                if (bytesRead > 0) {
                    File chunkFile = new File(sourceWavFile.getAbsolutePath().replace(
                            ".wav", "-%d.wav".formatted(chunkCounter)));
                    try (var partStream = new AudioInputStream(
                            new ByteArrayInputStream(buffer, 0, bytesRead),
                            format,
                            framesInThisFile)) {
                        AudioSystem.write(partStream, AudioFileFormat.Type.WAVE, chunkFile);
                    }
                    chunks.add(chunkFile);
                    chunkCounter++;
                }
                totalFrames -= framesInThisFile;
            }
        } catch (UnsupportedAudioFileException | IOException e) {
            throw new RuntimeException(e);
        }

        return chunks;
    }
}